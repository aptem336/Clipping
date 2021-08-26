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
    private final FloatBuffer polygonColor = FloatBuffer.wrap(new float[]{0.0F, 0.0F, 0.0F});//цвет отсекающего полигона
    private final FloatBuffer clippedSectionColor = FloatBuffer.wrap(new float[]{0.0F, 0.0F, 0.0F});//цвет отсеченной части отрезка
    private final FloatBuffer unclippedSectionColor = FloatBuffer.wrap(new float[]{1.0F, 0.0F, 0.0F});//цвет не отсеченной части отрезка
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
        //доабвление слушателей GL и мыщи - инстанса самого класса
        Clipping clipping = new Clipping();
        canvas.addGLEventListener(clipping);
        canvas.addMouseListener(clipping);
        //добавление канвы на фрейм
        frame.add(canvas);
        final Animator animator = new Animator(canvas);
        //добавление слушателя закрытия окна
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
        gl.glColor3fv(polygonColor);
        for (int i = 0; i < polygonVectors.size(); i++) {
            //вектор начала РЕБРА
            Vector edgeVectorA = polygonVectors.get(i);
            //вектор конца РЕБРА
            Vector edgeVectorB = polygonVectors.get((i + 1) % polygonVectors.size());
            //отрисовка РЕБРА
            gl.glVertex2f(edgeVectorA.getX(), drawable.getSurfaceHeight() - edgeVectorA.getY());
            gl.glVertex2f(edgeVectorB.getX(), drawable.getSurfaceHeight() - edgeVectorB.getY());
        }
        gl.glColor3fv(clippedSectionColor);
        for (int j = 0; j < sectionVectors.size() - 1; j += 2) {
            //вектор начала ОТРЕЗКА
            Vector sectionVectorA = sectionVectors.get(j);
            //вектор конца ОТРЕЗКА
            Vector sectionVectorB = sectionVectors.get(j + 1);
            //отрисовка ОТРЕЗКА
            gl.glVertex2f(sectionVectorA.getX(), drawable.getSurfaceHeight() - sectionVectorA.getY());
            gl.glVertex2f(sectionVectorB.getX(), drawable.getSurfaceHeight() - sectionVectorB.getY());
        }
        gl.glColor3fv(unclippedSectionColor);
        for (int i = 0; i < polygonVectors.size(); i++) {
            //вектор начала РЕБРА
            Vector edgeVectorA = polygonVectors.get(i);
            //вектор конца РЕБРА
            Vector edgeVectorB = polygonVectors.get((i + 1) % polygonVectors.size());
            //вектор РЕБРА
            Vector edgeVector = edgeVectorB.difference(edgeVectorA);
            //левая нормаль РЕБРА
            Vector edgeNormal = edgeVector.leftNormal();
            for (int j = 0; j < sectionVectors.size() - 1; j += 2) {
                //вектор начала ОТРЕЗКА
                Vector sectionVectorA = sectionVectors.get(j);
                //вектор конца ОТРЕЗКА
                Vector sectionVectorB = sectionVectors.get(j + 1);
                //вектор ОТРЕЗКА
                Vector sectionVector = sectionVectorB.difference(sectionVectorA);
                //Каждый отсекаемый отрезок содержит координаты начала и конца, а также два параметра tA и tB, соответствующие началу и концу отрезка.
                float tA = 0.0f;
                float tB = 1.0f;
                //Для каждого ребра E вычисляется параметр tE, описывающий пересечение E с прямой, на которой лежит отрезок P.
                float tE = edgeVector.crossProduct(edgeVectorA.difference(sectionVectorA)) / edgeVector.crossProduct(sectionVector);
                //Вычисляется скалярное произведение вектора E и внешней нормали N, в зависимости от знака которого возникает одна из следующих ситуаций:
                float dotProduct = edgeNormal.dotProduct(sectionVector);
                switch (Integer.compare((int) dotProduct, 0)) {
                    //E · N < 0 — отрезок P направлен с внутренней на внешнюю сторону ребра E. В этом случае параметр tA заменяется на tE, если tE > tA.
                    case -1:
                        if (tE > tA) {
                            tA = tE;
                        }
                        break;
                    //E · N > 0 — отрезок P направлен с внешней на внутреннюю сторону ребра E. В этом случае параметр tB заменяется на tE, если tE < tB.
                    case 1:
                        if (tE < tB) {
                            tB = tE;
                        }
                        break;
                    //E · N = 0 — отрезок P параллелен ребру E. Отрезок P отбрасывается как невидимый, если находится по правую сторону от E.
                    case 0:
                        if (edgeVector.crossProduct(edgeVectorB.difference(sectionVectorA)) < 0) continue;
                        break;
                }
                //Если tA <  tB, то заданная параметрами tA и tB часть отрезка P видима. В противном случае отрезок P полностью невидим.
                if (tA < tB) {
                    //вектор начала неотсеченного куска ОТРЕЗКА
                    Vector unclippedSectionA = sectionVectorA.sum(sectionVectorB.difference(sectionVectorA).product(tA));
                    //вектор конца неотсеченного куска ОТРЕЗКА
                    Vector unclippedSectionB = sectionVectorA.sum(sectionVectorB.difference(sectionVectorA).product(tB));
                    //отрисовка неотсеченного куска ОТРЕЗКА
                    gl.glVertex2f(unclippedSectionA.getX(), drawable.getSurfaceHeight() - unclippedSectionA.getY());
                    gl.glVertex2f(unclippedSectionB.getX(), drawable.getSurfaceHeight() - unclippedSectionB.getY());
                }
            }
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
