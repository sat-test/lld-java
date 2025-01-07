package SnakeLadderGame;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

class Player {
    private final Integer playerId;
    private final String name;
    private Integer position;

    public Player(Integer playerId, String name, Integer position) {
        this.playerId = playerId;
        this.name = name;
        this.position = position;
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public String getName() {
        return name;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}

class Jump {
    private final Integer start;
    private final Integer end;

    public Jump(Integer start, Integer end) {
        this.start = start;
        this.end = end;
    }

    public Integer getStart() {
        return start;
    }

    public Integer getEnd() {
        return end;
    }
}

class Snake extends Jump {
    public Snake(Integer start, Integer end) {
        super(start, end);
    }
}

class Ladder extends Jump {
    public Ladder(Integer start, Integer end) {
        super(start, end);
    }
}

class Cell {
    private Jump jump;
    public Cell() {
        jump = null;
    }

    public void addJump(Jump jump) {
        this.jump = jump;
    }

    public Jump getJump() {
        return jump;
    }
}

class Board {
    private final Map<Integer, Cell> cells = new HashMap<>();
    private final int size;

    public Board(int size) {
        this.size = size;
        initializeBoard();
    }

    private void initializeBoard() {
        for (int i = 1; i <= size * size; i++) {
            cells.put(i, new Cell());
        }
    }

    public void addSnake(int position, Jump jump) {
        if (!cells.containsKey(position)) {
            throw new IllegalArgumentException("Invalid cell position");
        }
        cells.get(position).addJump(jump);
    }

    public void addLadder(int position, Jump jump) {
        if (!cells.containsKey(position)) {
            throw new IllegalArgumentException("Invalid cell position");
        }
        cells.get(position).addJump(jump);
    }

    public Cell getCell(int position) {
        return cells.getOrDefault(position, new Cell());
    }

    public int getLastPosition() {
        return size * size;
    }
}

class Dice {
    Integer diceCount;
    int minVal = 1;
    int maxVal = 6;

    public Dice(Integer diceCount) {
        this.diceCount = diceCount;
    }

    public int rollDice() {
        int totalSum = 0;
        int diceUsed = 0;
        while(diceUsed < diceCount) {
            totalSum += ThreadLocalRandom.current().nextInt(minVal, maxVal+1);
            diceUsed++;
        }
        return totalSum;
    }
}

class Game {
    private Board board;
    private Dice dice;
    private Deque<Player> players;
    private Player winner;

    public Game(Board board, Dice dice, List<Player> playerList) {
        this.board = board;
        this.dice = dice;
        this.players = new LinkedList<>(playerList);
    }

    public void play() {
        while (winner == null) {
            Player currentPlayer = players.poll();
            int diceValue = dice.rollDice();
            int newPosition = updatePlayerPosition(currentPlayer, diceValue);

            System.out.println(currentPlayer.getName() + " rolled " + diceValue + " and moved to position " + newPosition);

            if (newPosition == board.getLastPosition()) {
                winner = currentPlayer;
                System.out.println("Winner is " + winner.getName());
                break;
            }

            players.offer(currentPlayer);
        }
    }

    private int updatePlayerPosition(Player player, int diceValue) {
        int newPosition = player.getPosition() + diceValue;
        if (newPosition > board.getLastPosition()) {
            return player.getPosition(); // No movement
        }
        Cell cell = board.getCell(newPosition);
        if (cell.getJump() != null) {
            if(cell.getJump().getStart() > cell.getJump().getEnd()) {
                System.out.println(player.getName() + " got snake bit and move from " + cell.getJump().getStart() + " to " + cell.getJump().getEnd());
            } else {
                System.out.println(player.getName() + " got ladder and move from " + cell.getJump().getStart() + " to " + cell.getJump().getEnd());
            }
            newPosition = cell.getJump().getEnd();
        }
        player.setPosition(newPosition);
        return newPosition;
    }
}

public class SnakeLadderGame {
    public static void main(String[] args) {
        Board board = new Board(10);
        board.addSnake(14, new Snake(14, 7));
        board.addSnake(55, new Snake(55, 17));
        board.addLadder(45, new Ladder(45, 98));
        board.addLadder(10, new Ladder(10, 30));
        board.addLadder(3, new Ladder(3, 22));

        Dice dice = new Dice(1);

        List<Player> players = new ArrayList<Player>() {{
            add(new Player(1, "Aman", 0));
            add(new Player(2, "Kamal", 0));
        }};

        Game game = new Game(board, dice, players);
        game.play();
    }
}
