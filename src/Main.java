import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame("Raytracing Java");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        rayTracingPanel panel = new rayTracingPanel();
        window.add(panel);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        window.setSize(900,600);

        panel.setBackground(Color.BLACK);
        panel.startThread();
    }
}

class rayTracingPanel extends JPanel implements Runnable {
    public rayTracingPanel(){
        addMouseListener(mouseHandler);
    }

    Thread thread;
    MouseHandler mouseHandler = new MouseHandler();
    Circle circle = new Circle(200,200,20);
    Circle oCircle = new Circle(700 ,300,40);
    Circle oCircle2 = new Circle(500,100,20);

    public void startThread(){
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        double drawInterval = (double) 1000000000 /60;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        int timer = 0;
        int drawCount = 0;

        while(thread != null){
            currentTime = System.nanoTime();

            delta += (currentTime - lastTime) / drawInterval;
            timer += (int) (currentTime - lastTime);
            lastTime = currentTime;

            if(delta >= 1){
                repaint();
                delta--;
                drawCount++;
            }
            if(timer >= 1000000000){
                drawCount = 0;
                timer = 0;
            }
        }
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        circle.g2 = g2;
        oCircle.g2 = g2;
        oCircle2.g2 = g2;

        Circle[] circles = new Circle[2];
        circles[0] = oCircle;
        circles[1] = oCircle2;

        Point mousePos = getMousePosition();
        if (mouseHandler.mousePressed && mousePos != null) {
            circle.x = mousePos.x;
            circle.y = mousePos.y;
        }
        oCircle.moveCircle(true,true,3);
        oCircle2.moveCircle(true, false, 7);
        oCircle.circleCollision(circles);
        oCircle2.circleCollision(circles);
        oCircle.circleCollision(circle);
        oCircle2.circleCollision(circle);
        circle.generateRays(100);
        circle.fillRays(circles);


        circle.drawCircle();
    }
}

class Ray{
    double x, y;
    double angle;

    public Ray(double x, double y, double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
    }
}

class MouseHandler implements MouseListener{
    boolean mousePressed;


    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            mousePressed = true;
            System.out.println("Left mouse pressed detected! mousePressed = " + mousePressed);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            mousePressed = false;
            System.out.println("Left mouse released detected! mousePressed = " + mousePressed);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}

class Circle {
    double x;
    double y;
    double radius;
    Graphics g2;
    Ray[] rays;

    private int directionX = 1;
    private int directionY = 1;

    public Circle(double x, double y, double radius){
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    void drawCircle(){
        double radius_squared = Math.pow(this.radius,2);
        for(double x = this.x - this.radius; x <= this.x+radius;x++){
            for(double y = this.y - this.radius; y <= this.y+radius; y++){
                double distance_squared = Math.pow(x - this.x, 2) + Math.pow(y - this.y, 2);
                if(distance_squared < radius_squared){
                    this.g2.setColor(Color.WHITE);
                    this.g2.fillRect((int) x,(int) y,1,1);
                }
            }
        }
    }

    void circleCollision(Circle[] objects){
        for(Circle circle : objects){
            if(Math.pow(this.radius - circle.radius,2) <= Math.pow(this.x - circle.x,2) + Math.pow(this.y - circle.y,2)
                    && Math.pow(this.x - circle.x,2) + Math.pow(this.y - circle.y,2) <= Math.pow(this.radius + circle.radius,2)
                    && this != circle){
                directionX = -directionX;
                directionY = -directionY;
            }
        }
    }

    void circleCollision(Circle circle){
        if(Math.pow(this.radius - circle.radius,2) <= Math.pow(this.x - circle.x,2) + Math.pow(this.y - circle.y,2)
                && Math.pow(this.x - circle.x,2) + Math.pow(this.y - circle.y,2) <= Math.pow(this.radius + circle.radius,2)
                && this != circle){
            directionX = -directionX;
            directionY = -directionY;
        }
    }

    void moveCircle(boolean moveX, boolean moveY, int speed){
        if(moveX) {
            if (this.x >= g2.getClipBounds().getWidth() || this.x <= 0) {
                directionX = -directionX;
            }
            this.x += speed * directionX;

        }
        if(moveY){
            if (this.y >= g2.getClipBounds().getHeight() || this.y <= 0 ) {
                directionY = -directionY;
            }
            this.y += speed * directionY;
        }
        this.drawCircle();

    }

    void generateRays(int numRays){
        this.rays = new Ray[numRays];

        for(int i=0; i<rays.length; i++){
            double angle = ((double) i / rays.length) * 2 * Math.PI;
            Ray ray = new Ray(this.x, this.y, angle);
            rays[i] = ray;
        }
    }

    void fillRays(Circle[] objects){
        if(this.rays == null) return;
        for(int i=0; i<rays.length; i++){
            Ray ray = this.rays[i];

            boolean end_of_screen = false;
            boolean object_hit = false;
            int step = 1;

            double drawX = ray.x;
            double drawY = ray.y;
            while(!end_of_screen) {
                drawX += step * Math.cos(ray.angle);
                drawY += step * Math.sin(ray.angle);

                if (drawX < 0 || drawX >= g2.getClipBounds().getWidth()|| drawY < 0 || drawY >= g2.getClipBounds().getHeight()){
                    end_of_screen = true;
                }

                for (Circle object : objects) {
                    double radius_squared = Math.pow(object.radius, 2);
                    double distance_squared = Math.pow(drawX - object.x, 2) + Math.pow(drawY - object.y, 2);
                    if (distance_squared < radius_squared) {
                        object_hit = true;
                    }
                }
                if (!object_hit) {
                    this.g2.setColor(Color.YELLOW);
                    this.g2.fillRect((int) drawX, (int) drawY, 1, 1);
                }
            }
        }
    }
}

