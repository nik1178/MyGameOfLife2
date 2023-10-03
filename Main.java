import java.util.*;
import java.io.*;
import java.nio.file.*;

// Does quite work btw, but hashmaps, and it decently fast

public class Main {
    public static void main(String[] args) {
        try {
            File file = new File("board");
            String board = Files.readString(file.toPath());
            Game game = new Game(board);

            long drawTime = 0;
            long updateTime = 0;
            int limit = 500;
            while (limit--!=0) {
                long time = System.nanoTime();
                game.printBoard();
                drawTime += System.nanoTime() - time;
                time = System.nanoTime();
                game.updateBoard();
                updateTime += System.nanoTime() - time;
            }
            System.out.println("Draw time: " + drawTime/1000000 + "ms");
            System.out.println("Update time: " + updateTime/1000000 + "ms");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

class Game {
    int width = -1;
    int height = 0;
    Board board;
    Board secondBoard;
    int generation = 0;


    public Game(String tempBoard) {
        getBoardData(tempBoard);
    }

    private void getBoardData(String tempBoard) {
        int tempWidth = 0;
        for (int i = 0; i < tempBoard.length(); i++) {
            char current = tempBoard.charAt(i);
            if (current == '.' || current == 'X') {
                tempWidth++;
            } else if (current == '\n') {
                if (this.width == -1) {
                    this.width = tempWidth;
                } else if (this.width != tempWidth) {
                    System.out.println("Error: Board width is not consistent");
                    System.exit(1);
                }
                this.height++;
                tempWidth = 0;
            }
        }

        board = new Board(this.width, this.height);
        secondBoard = new Board(this.width, this.height);

        tempWidth = 0;
        int tempHeight = 0;
        for (int i=0; i<tempBoard.length(); i++) {
            char current = tempBoard.charAt(i);
            switch (current) {
                case 'X':
                    board.addCell(tempWidth, tempHeight);
                case '.' :
                    tempWidth++;
                    break;
                case '\n':
                    tempWidth = 0;
                    tempHeight++;
                    break;
                default: break;
            }
        }
    }

    public void updateBoard() {
        this.board.updateBoard(secondBoard);
        switchBoards();
    }

    private void switchBoards() {
        Board temp = this.board;
        this.board = this.secondBoard;
        this.secondBoard = temp;
        this.secondBoard.clearBoard();
    }

    public void printBoard() {
        StringBuilder sb = new StringBuilder();
        for (int y=0; y<this.height; y++) {
            for (int x=0; x<this.width; x++) {
                if (this.board.board.get(y).get(x) != null && this.board.board.get(y).get(x)) {
                    sb.append('X');
                } else {
                    sb.append('.');
                }
            }
            sb.append('\n');
        }
        System.out.println(sb.toString() + generation++);
    }
}

class Board {
    HashMap<Integer, HashMap<Integer, Boolean>> board; // y, x, isAlive ;; All alive cells and their neighbours
    LinkedHashSet<Integer> usedLines; // in which lines we actually added cells
    int width = -1;
    int height = 0;

    Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.board = new HashMap<>(height);
        this.usedLines = new LinkedHashSet<>(height);
        for (int y=0; y<height; y++) {
            HashMap<Integer, Boolean> temp = new HashMap<>(width);
            this.board.put(y, temp);
        }
    }

    public void addCell(int x, int y) {
        if (x<0 || y<0 || x>=this.width || y>=this.height) {
            return;
        }

        this.board.get(y).put(x, true);

        for (int i=-1; i<=1; i++) {
            int tempY = y+i;
            if (tempY<0 || tempY>=this.height) {
                continue;
            }
            this.usedLines.add(tempY);
            for (int j=-1; j<=1; j++) {
                int tempX = x+j;
                if (tempX<0 || tempX>=this.width) {
                    continue;
                }
                if (this.board.get(tempY).get(tempX) == null) {
                    this.board.get(tempY).put(tempX, false);
                }
            }
        }
    }

    public void clearBoard() {
        for (int y : this.usedLines) {
            this.board.get(y).clear();
        }
        this.usedLines.clear();
    }

    public void updateBoard(Board secondBoard) {
        for (int y : this.usedLines) {
            for (int x : board.get(y).keySet()) {
                int neighbours = 0;
                for (int i=-1; i<=1; i++) {
                    int tempY = y+i;
                    if (tempY<0 || tempY>=this.height) {
                        continue;
                    }
                    for (int j=-1; j<=1; j++) {
                        int tempX = x+j;
                        if (tempX<0 || tempX>=this.width) {
                            continue;
                        }
                        if (this.board.get(tempY).get(tempX)!=null && this.board.get(tempY).get(tempX)) {
                            neighbours++;
                        }
                    }
                }
                if (this.board.get(y).get(x)) {
                    neighbours--;
                }
                if (neighbours == 3) {
                    secondBoard.addCell(x, y);
                    continue;
                }
                if (neighbours == 2) {
                    if (this.board.get(y).get(x)) {
                        secondBoard.addCell(x, y);
                        continue;
                    }
                }
            }
        }
    }
}