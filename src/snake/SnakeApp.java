package snake;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.Random;

public class SnakeApp extends Application {
    // 固定常量
    private static final int WIDTH = 30;
    private static final int HEIGHT = 20;
    private static final int CORNER_SIZE = 40;

    // 方向
    private static final int UP = 1;
    private static final int LEFT = 2;
    private static final int DOWN = 3;
    private static final int RIGHT = 4;

    // 游戏数据
    private static int speed = 3;
    private static int score = 0;
    private static int direction = LEFT;
    // 食物坐标
    private static final Point food = new Point(-1, -1);
    // 蛇
    private static final Point[] snake = new Point[300];
    private static int snakeLength = 0;
    // 随机数生成器 —— 用于随机生成食物坐标
    private static final Random random = new Random();
    // 指示游戏是否结束
    private static boolean gameOver = false;
    // 指示游戏是否暂停
    private static boolean pause = false;

    // 生成随机的食物
    private static void newFood() {
        // 生成 x 的范围需要在 [0, WIDTH)
        // 生成 y 的范围需要在 [0, HEIGHT)
        // 不能和蛇的任何一点重合

        int x, y;
        do {
            x = random.nextInt(WIDTH);
            y = random.nextInt(HEIGHT);
        } while (isCollision(x, y));

        food.x = x;
        food.y = y;
    }

    // 判断是否和蛇右任何一点重合了
    private static boolean isCollision(int x, int y) {
        for (int i = 0; i < snakeLength; i++) {
            if (snake[i].x == x && snake[i].y == y) {
                return true;
            }
        }

        return false;
    }

    // 初始化游戏数据
    private static void resetGame() {
        speed = 3;
        score = 0;
        direction = LEFT;
        newFood();
        Arrays.fill(snake, null);
        snakeLength = 0;

        snake[snakeLength++] = new Point(WIDTH / 2, HEIGHT / 2);
        snake[snakeLength++] = new Point(WIDTH / 2, HEIGHT / 2);
        snake[snakeLength++] = new Point(WIDTH / 2, HEIGHT / 2);

        gameOver = false;
        pause = false;
    }

    // 每个周期要做的事情
    private static void period() {
        // 移动蛇 —— 先处理不是蛇头的部分
        for (int i = snakeLength - 1; i >= 1; i--) {
            // 让蛇的每一点都跟着它的前一个点
            snake[i].x = snake[i - 1].x;
            snake[i].y = snake[i - 1].y;
        }
        // 移动蛇 —— 再处理蛇头的部分
        // 坐标系（x 朝右；y 朝下）
        switch (direction) {
            case UP:
                snake[0].y--;
                break;
            case DOWN:
                snake[0].y++;
                break;
            case LEFT:
                snake[0].x--;
                break;
            case RIGHT:
                snake[0].x++;
                break;
        }

        // 判断是不是撞墙了
        if (snake[0].x < 0 || snake[0].x >= WIDTH || snake[0].y < 0 || snake[0].y >= HEIGHT) {
            gameOver = true;
            return;
        }

        // 判断蛇头有没有撞到自己身体的其他位置
        for (int i = 1; i < snakeLength; i++) {
            if (snake[0].x == snake[i].x && snake[0].y == snake[i].y) {
                gameOver = true;
                return;
            }
        }

        // 判断有没有吃到食物
        if (food.x == snake[0].x && food.y == snake[0].y) {
            // 蛇生长
            snake[snakeLength++] = new Point(snake[0].x, snake[0].y);   // 这里其实加到哪里都无所谓，因为一移动就正常了
            // 生成新食物
            newFood();
            // 分数增加
            score++;
            // 达到一定级别，速度增加
            if (score % 10 == 0) {
                speed++;
            }
        }
    }

    // 画图
    private static void render(GraphicsContext gc) {
        // 背景
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WIDTH * CORNER_SIZE, HEIGHT * CORNER_SIZE);

        // 分数
        gc.setFill(Color.WHITE);
        gc.setFont(new Font(30));
        gc.fillText("分数: " + score, 30, 30);

        // 食物
        gc.setFill(Color.LIGHTSALMON);
        gc.fillOval(food.x * CORNER_SIZE + 3, food.y * CORNER_SIZE + 3, CORNER_SIZE - 6, CORNER_SIZE - 6);
        gc.setFill(Color.YELLOW);
        gc.fillOval(food.x * CORNER_SIZE + 7, food.y * CORNER_SIZE + 7, CORNER_SIZE - 14, CORNER_SIZE - 14);

        // 蛇
        for (int i = 0; i < snakeLength; i++) {
            Point point = snake[i];
            gc.setFill(Color.WHITE);
            gc.fillRect(point.x * CORNER_SIZE + 1, point.y * CORNER_SIZE + 1, CORNER_SIZE - 2, CORNER_SIZE - 2);
            gc.setFill(Color.LIGHTGREEN);
            gc.fillRect(point.x * CORNER_SIZE + 3, point.y * CORNER_SIZE + 3, CORNER_SIZE - 6, CORNER_SIZE - 6);
        }

        // 游戏结束
        if (gameOver) {
            gc.setFill(Color.RED);
            gc.setFont(new Font(40));
            gc.fillText("游戏结束，按 R 重新开始", 30, 300);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(WIDTH * CORNER_SIZE, HEIGHT * CORNER_SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        StackPane root = new StackPane();
        root.getChildren().add(canvas);
        Scene scene = new Scene(root);

        AnimationTimer timer = new AnimationTimer() {
            long lastTick;

            @Override
            public void handle(long now) {
                if (gameOver) {
                    return;
                }

                if (lastTick == 0 || now - lastTick > 1_000_000_000 / speed) {
                    lastTick = now;

                    period();
                    render(gc);
                }
            }
        };

        resetGame();
        scene.setOnKeyPressed(new MyKeyHandler(timer));
        timer.start();

        primaryStage.setScene(scene);
        primaryStage.setTitle("贪吃蛇");
        primaryStage.setResizable(false);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    private static class MyKeyHandler implements EventHandler<KeyEvent> {
        private final AnimationTimer timer;

        MyKeyHandler(AnimationTimer timer) {
            this.timer = timer;
        }

        @Override
        public void handle(KeyEvent event) {
            switch (event.getCode()) {
                case W: case UP:
                    if (direction != DOWN) {
                        direction = UP;
                    }
                    break;
                case A: case LEFT:
                    if (direction != RIGHT) {
                        direction = LEFT;
                    }
                    break;
                case S: case DOWN:
                    if (direction != UP) {
                        direction = DOWN;
                    }
                    break;
                case D: case RIGHT:
                    if (direction != LEFT) {
                        direction = RIGHT;
                    }
                    break;
                case R:
                    if (gameOver) {
                        resetGame();
                    }
                    break;
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
