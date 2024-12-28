import java.util.HashMap;
import java.util.UUID;
import java.lang.Thread;

enum VehicleType {
    BIKE,
    CAR,
    TRUCK
}

enum ParkingSpotType {
    COMPACT,
    LARGE,
    HANDICAPPED,
    BIKE
}

abstract class Vehicle {
    private String licensePlate;
    private VehicleType vehicleType;
    
    public Vehicle(String licensePlate, VehicleType vehicleType) {
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
    }
    
    public String getLicensePlate() {
        return licensePlate;
    }
    
    public VehicleType getVehicleType() {
        return vehicleType;
    }
}

class Bike extends Vehicle {
    public Bike(String licensePlate) {
        super(licensePlate, VehicleType.BIKE);
    } 
}

class Car extends Vehicle {
    public Car(String licensePlate) {
        super(licensePlate, VehicleType.CAR);
    } 
}

class Truck extends Vehicle {
    public Truck(String licensePlate) {
        super(licensePlate, VehicleType.TRUCK);
    } 
}

class ParkingSpot {
    private String id;
    private ParkingSpotType type;
    private Vehicle vehicle;
    private Boolean isAvailable;
    
    public ParkingSpot(String id, ParkingSpotType type) {
        this.id = id;
        this.type = type;
        this.isAvailable = true;
    }
    
    public Boolean isAvailable() {
        return isAvailable;
    }
    
    public Boolean canPark(Vehicle vehicle) {
        return (type == ParkingSpotType.COMPACT && vehicle.getVehicleType() == VehicleType.CAR) 
            || (type == ParkingSpotType.BIKE && vehicle.getVehicleType() == VehicleType.BIKE)
            || (type == ParkingSpotType.LARGE && vehicle.getVehicleType() == VehicleType.TRUCK);
    }
    
    public Boolean park(Vehicle vehicle) {
        if(isAvailable && canPark(vehicle)) {
            this.vehicle = vehicle;
            this.isAvailable = false;
            return true;
        }
        return false;
    }
    
    public void unPark() {
        vehicle = null;
        isAvailable = true;
    }
    
    public String getId() {
        return id;
    }
    
    public ParkingSpotType getType() {
        return type;
    }
}

class ParkingFloor {
    private String id;
    private HashMap<ParkingSpotType, Integer> availableSpots;
    private HashMap<String, ParkingSpot> spots;
    
    public ParkingFloor(String id) {
        this.id = id;
        this.availableSpots = new HashMap<>();
        this.spots = new HashMap<>();
    }
    
    public void addSpot(ParkingSpot spot) {
        spots.put(spot.getId(), spot);
        availableSpots.put(spot.getType(), availableSpots.getOrDefault(spot.getType(), 0)+1);
    }
    
    public ParkingSpot findAndPark(Vehicle vehicle) {
        for(ParkingSpot spot : spots.values()) {
            if(spot.isAvailable() && spot.park(vehicle)) {
                spot.park(vehicle);
                availableSpots.put(spot.getType(), availableSpots.get(spot.getType()) - 1);
                return spot;
            }
        }
        return null;
    }
    
    public HashMap<String, ParkingSpot> getParkingSpots() {
        return spots;
    }
}

class ParkingLot {
    String name;
    List<ParkingFloor> floors;
    
    public ParkingLot(String name) {
        this.name = name;
        floors = new ArrayList<>();
    }
    
    public void addFloor(ParkingFloor floor) {
        this.floors.add(floor);
    }
    
    public ParkingSpot parkVehicle(Vehicle vehicle) {
        for (ParkingFloor floor : floors) {
            ParkingSpot spot = floor.findAndPark(vehicle);
            if (spot != null) {
                return spot;
            }
        }
        return null; 
    }
    
    public ParkingSpot getSpotById(String spotId) {
        for (ParkingFloor floor : floors) {
            for (ParkingSpot spot : floor.getParkingSpots().values()) {
                if (spot.getId().equals(spotId)) {
                    return spot;
                }
            }
        }
        return null; 
    }
}

class Ticket {
    private String ticketId;
    private String parkingSpotId;
    private long startTime;
    private long endTime;
    
    public Ticket(String ticketId, String parkingSpotId, long startTime) {
        this.ticketId = ticketId;
        this.parkingSpotId = parkingSpotId;
        this.startTime = startTime;
    }
    
    public void endParking(long endTime) {
        this.endTime = endTime;
    }
    
    public long calculateFee() {
        long duration = endTime - startTime;
        return duration * 10; 
    }
    
    public String getParkingSpotId() {
        return parkingSpotId;
    }
    
    public String getTicketId() {
        return ticketId;
    }
}


class ParkingManager {
    private ParkingLot parkingLot;
    private HashMap<String, Ticket> activeTickets;
    
    public ParkingManager(ParkingLot parkingLot) {
        this.parkingLot = parkingLot;
        this.activeTickets = new HashMap<>();
    }
    
    /**
     * Parks a vehicle in the parking lot and issues a ticket.
     *
     * @param vehicle the vehicle to be parked
     * @return the issued Ticket
     */
    public Ticket parkVehicle(Vehicle vehicle) {
        ParkingSpot assignedSpot = parkingLot.parkVehicle(vehicle);
        
        if (assignedSpot == null) {
            System.out.println("No parking spot available for vehicle type: " + vehicle.getVehicleType());
            return null;
        }
        
        // Generate a new ticket
        String ticketId = UUID.randomUUID().toString();
        Ticket ticket = new Ticket(ticketId, assignedSpot.getId(), System.currentTimeMillis());
        activeTickets.put(ticketId, ticket);
        
        System.out.println("Vehicle parked. Ticket ID: " + ticketId);
        return ticket;
    }
    
    /**
     * Unparks a vehicle using the provided ticket and calculates the fee.
     *
     * @param ticketId the ID of the parking ticket
     * @return the parking fee
     */
    public long unparkVehicle(String ticketId) {
        Ticket ticket = activeTickets.get(ticketId);
        
        if (ticket == null) {
            System.out.println("Invalid Ticket ID.");
            return -1;
        }
        
        ParkingSpot spot = parkingLot.getSpotById(ticket.getParkingSpotId());
        if (spot != null) {
            spot.unPark();
        }
        
        ticket.endParking(System.currentTimeMillis());
        long fee = ticket.calculateFee();
        
        activeTickets.remove(ticketId);
        
        // System.out.println("Vehicle unparked. Parking fee: " + fee);
        return fee;
    }
}

public class Main {
    public static void main(String[] args) {
        // Create ParkingLot and ParkingManager
        ParkingLot parkingLot = new ParkingLot("Downtown Parking");
        ParkingManager manager = new ParkingManager(parkingLot);
        
        // Setup ParkingLot
        ParkingFloor floor1 = new ParkingFloor("Floor 1");
        floor1.addSpot(new ParkingSpot("C11", ParkingSpotType.COMPACT));
        floor1.addSpot(new ParkingSpot("C12", ParkingSpotType.COMPACT));
        floor1.addSpot(new ParkingSpot("B11", ParkingSpotType.BIKE));
        floor1.addSpot(new ParkingSpot("B12", ParkingSpotType.BIKE));
        parkingLot.addFloor(floor1);
        
         // Setup ParkingLot
        ParkingFloor floor2 = new ParkingFloor("Floor 2");
        floor2.addSpot(new ParkingSpot("C21", ParkingSpotType.COMPACT));
        floor2.addSpot(new ParkingSpot("C22", ParkingSpotType.COMPACT));
        floor2.addSpot(new ParkingSpot("B21", ParkingSpotType.BIKE));
        floor2.addSpot(new ParkingSpot("B22", ParkingSpotType.BIKE));
        parkingLot.addFloor(floor2);
        
        // Park vehicles
        Vehicle car1 = new Car("CAR1");
        Ticket carTicket1 = manager.parkVehicle(car1);
        
        Vehicle car2 = new Car("CAR2");
        Ticket carTicket2 = manager.parkVehicle(car2);
        
        Vehicle car3 = new Car("CAR3");
        Ticket carTicket3 = manager.parkVehicle(car3);
        
        Vehicle car4 = new Car("CAR4");
        Ticket carTicket4 = manager.parkVehicle(car4);
        
        Vehicle car5 = new Car("CAR5");
        Ticket carTicket5 = manager.parkVehicle(car5);
        
        // // Unpark vehicles
        if (carTicket1 != null) {
            long fee = manager.unparkVehicle(carTicket1.getTicketId());
            System.out.println("Parking fee for CAR1: " + fee);
        }
        
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            
        }
        
        if (carTicket2 != null) {
            long fee = manager.unparkVehicle(carTicket2.getTicketId());
            System.out.println("Parking fee for CAR2: " + fee);
        }
        
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            
        }
        
        if (carTicket3 != null) {
            long fee = manager.unparkVehicle(carTicket3.getTicketId());
            System.out.println("Parking fee for CAR3: " + fee);
        }
        
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            
        }
        
        if (carTicket4 != null) {
            long fee = manager.unparkVehicle(carTicket4.getTicketId());
            System.out.println("Parking fee for CAR4: " + fee);
        }
        
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            
        }
        
        if (carTicket5 != null) {
            long fee = manager.unparkVehicle(carTicket5.getTicketId());
            System.out.println("Parking fee for CAR5: " + fee);
        }
    }
}
