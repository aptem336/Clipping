import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class Clipping implements GLEventListener, MouseListener {

    private final float[] clearColor = new float[]{0.0F, 0.0F, 0.0F, 1.0F};//цвет фона
    private final float[] polygonColor = new float[]{0.75F, 0.75F, 0.75F, 1.0F};//цвет отсекающего полигона
    private final float[] clippedSectionColor = new float[]{1.0F, 0.0F, 0.0F, 1.0F};//цвет отсеченной части отрезка
    private final float[] unclippedSectionColor = new float[]{1.0F, 1.0F, 1.0F, 1.0F};//цвет не отсеченной части отрезка
    private final List<Point> polygonPoints = new ArrayList<>();//массив точек отсекающего многоугольника
    private final List<Point> sectionPoints = new ArrayList<>();//массив точек отсекаемых отрезков

    public static void main(String[] args) {
        //инициализация фрейма
        JFrame frame = new JFrame();
        frame.setSize(1000, 1000);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        //инициализация канвы
        GLCanvas canvas = new GLCanvas();
        canvas.setSize(frame.getSize());
        canvas.setLocation(0, 0);
        //доабвление слушателей GL и мыщи - интанса самого класса
        Clipping clipping = new Clipping();
        canvas.addGLEventListener(clipping);
        canvas.addMouseListener(clipping);
        //добавление канвы на фрейм
        frame.add(canvas);
        final Animator animator = new Animator(canvas);
        //добавление слушаетля закрытия окна
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                new Thread(() -> {
                    //остановка аниматора
                    animator.stop();
                    //выход из программы
                    System.exit(0);
                }).start();
            }
        });
        //запуск аниматора
        animator.start();
    }

    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        //включение смешивания и его функции (для прозрачности)
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    }

    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        //очистка цветового буффера
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);

    }


    public void dispose(GLAutoDrawable glAutoDrawable) {
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        //ЛКМ - добавляем точку многоугольника
        if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
            polygonPoints.add(mouseEvent.getPoint());
        //ПКМ - добавляем точку отрезка
        } else if (mouseEvent.getButton() == MouseEvent.BUTTON2) {
            sectionPoints.add(mouseEvent.getPoint());
        //ПКМ - очищаем массивы точек
        } else if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
            polygonPoints.clear();
            sectionPoints.clear();
        }
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
    }
}