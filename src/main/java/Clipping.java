import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class Clipping implements GLEventListener, MouseListener {

    private final float[] clearColor = new float[]{0.25F, 0.25F, 0.25F, 1.0F};//цвет фона
    private final float[] polygonColor = new float[]{0.0F, 0.0F, 0.0F, 1.0F};//цвет отсекающего полигона
    private final float[] clippedSectionColor = new float[]{0.0F, 0.0F, 0.0F, 1.0F};//цвет отсеченной части отрезка
    private final float[] unclippedSectionColor = new float[]{1.0F, 0.0F, 0.0F, 1.0F};//цвет не отсеченной части отрезка
    private final List<Vector> polygonVectors = new ArrayList<>();//массив векторов отсекающего многоугольника
    private final List<Vector> sectionVectors = new ArrayList<>();//массив векторов отсекаемых отрезков

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
        //начала двумерной отрисовки
        gl.glOrtho(0, drawable.getSurfaceWidth(), 0, drawable.getSurfaceHeight(), 0, 1);
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    }

    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        //очистка цветового буффера
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        //очистка фона
        gl.glClearColor(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);
        //отрисовка отсекающего полигона
        gl.glBegin(GL2.GL_LINES);
        List<Vector> unclippedVectors = new ArrayList<>();
        for (int i = 0; i < polygonVectors.size(); i++) {
            //вектор начала РЕБРА
            Vector edgeVectorA = polygonVectors.get(i);
            //вектор конца РЕБРА
            Vector edgeVectorB = polygonVectors.get((i + 1) % polygonVectors.size());
            //отрисовка РЕБРА
            gl.glColor4fv(FloatBuffer.wrap(polygonColor));
            gl.glVertex2f(edgeVectorA.getX(), drawable.getSurfaceHeight() - edgeVectorA.getY());
            gl.glVertex2f(edgeVectorB.getX(), drawable.getSurfaceHeight() - edgeVectorB.getY());
            //вектор РЕБРА
            Vector edgeVector = edgeVectorB.difference(edgeVectorA);
            //левая нормаль РЕБРА
            Vector edgeNormal = edgeVector.leftNormal();
            gl.glColor4fv(FloatBuffer.wrap(clippedSectionColor));
            for (int j = 0; j < sectionVectors.size() - 1; j += 2) {
                //вектор начала ОТРЕЗКА
                Vector sectionVectorA = sectionVectors.get(j);
                //вектор конца ОТРЕЗКА
                Vector sectionVectorB = sectionVectors.get(j + 1);
                //отрисовка ОТРЕЗКА
                gl.glVertex2f(sectionVectorA.getX(), drawable.getSurfaceHeight() - sectionVectorA.getY());
                gl.glVertex2f(sectionVectorB.getX(), drawable.getSurfaceHeight() - sectionVectorB.getY());
                //вектор ОТРЕЗКА
                Vector sectionVector = sectionVectorB.difference(sectionVectorA);
                //начальное значение параметров t для начала и конца отрезка
                float tA = 0.0f;
                float tB = 1.0f;
                //СКАЛЯРНОЕ произведение нормали РЕБРА и вектора ОТРЕЗКА
                float dotProduct = edgeNormal.dotProduct(sectionVector);
                float t = edgeVector.crossProduct(edgeVectorA.difference(sectionVectorA)) / edgeVector.crossProduct(sectionVector);
                switch (Integer.compare((int) dotProduct, 0)) {
                    case -1:
                        if (t > tA) {
                            tA = t;
                        }
                        break;
                    case 1:
                        if (t < tB) {
                            tB = t;
                        }
                        break;
                    case 0:
                        break;
                }
                if (tA < tB) {
                    unclippedVectors.add(sectionVectorA.sum(sectionVectorB.difference(sectionVectorA).product(tA)));
                    unclippedVectors.add(sectionVectorA.sum(sectionVectorB.difference(sectionVectorA).product(tB)));
                }
            }
        }
        gl.glColor4fv(FloatBuffer.wrap(unclippedSectionColor));
        for (int i = 0; i < unclippedVectors.size() - 1; i += 2) {
            Vector unclippedSectorA = unclippedVectors.get(i);
            Vector unclippedSectorB = unclippedVectors.get(i + 1);
            gl.glVertex2f(unclippedSectorA.getX(), drawable.getSurfaceHeight() - unclippedSectorA.getY());
            gl.glVertex2f(unclippedSectorB.getX(), drawable.getSurfaceHeight() - unclippedSectorB.getY());
        }
        gl.glEnd();
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
        //ЛКМ - добавляем вектор многоугольника
        if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
            polygonVectors.add(new Vector(mouseEvent.getPoint()));
            //ПКМ - добавляем вектор отрезка
        } else if (mouseEvent.getButton() == MouseEvent.BUTTON2) {
            sectionVectors.add(new Vector(mouseEvent.getPoint()));
            //ПКМ - очищаем массивы векторов
        } else if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
            polygonVectors.clear();
            sectionVectors.clear();
        }
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
    }
}
