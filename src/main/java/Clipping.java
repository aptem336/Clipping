import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.Animator;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;

public class Clipping implements GLEventListener, MouseListener {

    private final static int TOP = 32;
    private final static int BOTTOM = 16;
    private final static int RIGHT = 8;
    private final static int LEFT = 4;
    private final static int FRONT = 2;
    private final static int BACK = 1;

    private final FloatBuffer areaColor = FloatBuffer.wrap(new float[]{1.0F, 0.0F, 0.0F});//цвет отсекающего полигона
    private final FloatBuffer clippedSectionColor = FloatBuffer.wrap(new float[]{1.0F, 1.0F, 1.0F});//цвет отсеченной части отрезка
    private final FloatBuffer unClippedSectionColor = FloatBuffer.wrap(new float[]{1.0F, 0.0F, 0.0F});//цвет не отсеченной части отрезка

    private final List<Vector> sectionVectors = Arrays.asList(
            new Vector((float) (Math.random() * 500 - 250), (float) (Math.random() * 500 - 250), (float) (Math.random() * 500 - 250)),
            new Vector((float) (Math.random() * 500 - 250), (float) (Math.random() * 500 - 250), (float) (Math.random() * 500 - 250)),
            new Vector((float) (Math.random() * 500 - 250), (float) (Math.random() * 500 - 250), (float) (Math.random() * 500 - 250)),
            new Vector((float) (Math.random() * 500 - 250), (float) (Math.random() * 500 - 250), (float) (Math.random() * 500 - 250)),
            new Vector((float) (Math.random() * 500 - 250), (float) (Math.random() * 500 - 250), (float) (Math.random() * 500 - 250))
    );//массив векторов отсекаемых отрезков
    private GL2 gl;
    private GLU glu;

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
        gl = drawable.getGL().getGL2();
        glu = new GLU();

        gl.glEnable(GL2.GL_COLOR_MATERIAL);

        gl.glEnable(GL.GL_BLEND);
        gl.glEnable(GL2.GL_ALPHA_TEST);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        final float h = (float) width / (float) height;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.0f, h, 1.0, 100000.0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    public void display(GLAutoDrawable drawable) {
        //очистка цветового буффера
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        gl.glTranslated(0d, 0d, 0d);

        glu.gluLookAt(200, 200, 400, 0d, 0d, 0d, 0d, 0.5d, 0d);

        Parallelepiped parallelepiped = new Parallelepiped(new Vector(50, 50, 50), new Vector(-50, -50, -50));
        sectionVectors.forEach(sectionVector -> clip(sectionVector,
                new Vector(-sectionVector.x, -sectionVector.y, -sectionVector.z),
                parallelepiped));
    }

    /**
     * Трёхмерное отсечение методом Коэна-Сазерленда.
     * begin - координаты начала отрезка
     * end - координаты конца отрезка
     * plane - координаты отсекающей области
     */
    private void clip(Vector begin, Vector end, Parallelepiped parallelepiped) {
        //отрисовка параллелепипеда
        gl.glLineWidth(1);
        gl.glColor3fv(areaColor);
        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glVertex3f(parallelepiped.max.x, parallelepiped.max.y, parallelepiped.max.z);
        gl.glVertex3f(parallelepiped.max.x, parallelepiped.min.y, parallelepiped.max.z);
        gl.glVertex3f(parallelepiped.min.x, parallelepiped.min.y, parallelepiped.max.z);
        gl.glVertex3f(parallelepiped.min.x, parallelepiped.max.y, parallelepiped.max.z);
        gl.glEnd();
        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glVertex3f(parallelepiped.max.x, parallelepiped.max.y, parallelepiped.min.z);
        gl.glVertex3f(parallelepiped.max.x, parallelepiped.min.y, parallelepiped.min.z);
        gl.glVertex3f(parallelepiped.min.x, parallelepiped.min.y, parallelepiped.min.z);
        gl.glVertex3f(parallelepiped.min.x, parallelepiped.max.y, parallelepiped.min.z);
        gl.glEnd();
        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glVertex3f(parallelepiped.max.x, parallelepiped.min.y, parallelepiped.max.z);
        gl.glVertex3f(parallelepiped.max.x, parallelepiped.min.y, parallelepiped.min.z);
        gl.glVertex3f(parallelepiped.min.x, parallelepiped.min.y, parallelepiped.min.z);
        gl.glVertex3f(parallelepiped.min.x, parallelepiped.min.y, parallelepiped.max.z);
        gl.glEnd();
        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glVertex3f(parallelepiped.max.x, parallelepiped.max.y, parallelepiped.max.z);
        gl.glVertex3f(parallelepiped.max.x, parallelepiped.max.y, parallelepiped.min.z);
        gl.glVertex3f(parallelepiped.min.x, parallelepiped.max.y, parallelepiped.min.z);
        gl.glVertex3f(parallelepiped.min.x, parallelepiped.max.y, parallelepiped.max.z);
        gl.glEnd();
        //отрисовка всего отрезка
        gl.glColor3fv(clippedSectionColor);
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(begin.x, begin.y, begin.z);
        gl.glVertex3f(end.x, end.y, end.z);
        gl.glEnd();
        //вычисляем начальные коды концов отрезка
        int outCodeBegin = getCode(begin, parallelepiped);
        int outCodeEnd = getCode(end, parallelepiped);

        Vector tempBegin = new Vector(begin.x, begin.y, begin.z);
        Vector tempEnd = new Vector(end.x, end.y, end.z);
        gl.glLineWidth(5);
        while (true) {
            if ((outCodeBegin | outCodeEnd) == 0) { //отрезок полностью видимый
                gl.glColor3fv(unClippedSectionColor);
                gl.glBegin(GL2.GL_LINES);
                gl.glVertex3f(tempBegin.x, tempBegin.y, tempBegin.z);
                gl.glVertex3f(tempEnd.x, tempEnd.y, tempEnd.z);
                gl.glEnd();
                break;
            } else if ((outCodeBegin & outCodeEnd) != 0) { //отрезок полностью невидимый
                break;
            } else { //отрезок частично видимый. Вычисление точек пересечения отрезка и области отсечения
                Vector vector = new Vector(0, 0, 0);
                int outCode = outCodeBegin != 0 ? outCodeBegin : outCodeEnd;
                if ((outCode & TOP) != 0) {
                    //тут прост интерполяция на основе нового значения y
                    vector.x = begin.x + (end.x - begin.x) * (parallelepiped.max.y - begin.y) / (end.y - begin.y);
                    vector.z = begin.z + (end.z - begin.z) * (parallelepiped.max.y - begin.y) / (end.y - begin.y);
                    //значение y = максимальному возможному
                    vector.y = parallelepiped.max.y;
                    //дальше по аналогии
                    //всё увязано else потому что значение уже поменялось (при вхождении в if) и флаги уже не верны
                } else if ((outCode & BOTTOM) != 0) {
                    vector.x = begin.x + (end.x - begin.x) * (parallelepiped.min.y - begin.y) / (end.y - begin.y);
                    vector.z = begin.z + (end.z - begin.z) * (parallelepiped.min.y - begin.y) / (end.y - begin.y);
                    vector.y = parallelepiped.min.y;
                } else if ((outCode & RIGHT) != 0) {
                    vector.y = begin.y + (end.y - begin.y) * (parallelepiped.max.x - begin.x) / (end.x - begin.x);
                    vector.z = begin.z + (end.z - begin.z) * (parallelepiped.max.x - begin.x) / (end.x - begin.x);
                    vector.x = parallelepiped.max.x;
                } else if ((outCode & LEFT) != 0) {
                    vector.y = begin.y + (end.y - begin.y) * (parallelepiped.min.x - begin.x) / (end.x - begin.x);
                    vector.z = begin.z + (end.z - begin.z) * (parallelepiped.min.x - begin.x) / (end.x - begin.x);
                    vector.x = parallelepiped.min.x;
                } else if ((outCode & FRONT) != 0) {
                    vector.x = begin.x + (end.x - begin.x) * (parallelepiped.max.z - begin.z) / (end.z - begin.z);
                    vector.y = begin.y + (end.y - begin.y) * (parallelepiped.max.z - begin.z) / (end.z - begin.z);
                    vector.z = parallelepiped.max.z;
                } else if ((outCode & BACK) != 0) {
                    vector.x = begin.x + (end.x - begin.x) * (parallelepiped.min.z - begin.z) / (end.z - begin.z);
                    vector.y = begin.y + (end.y - begin.y) * (parallelepiped.min.z - begin.z) / (end.z - begin.z);
                    vector.z = parallelepiped.min.z;
                }
                //укорачиваем пока не станет полностью видимым
                if (outCode == outCodeBegin) {
                    tempBegin.x = vector.x;
                    tempBegin.y = vector.y;
                    tempBegin.z = vector.z;
                    outCodeBegin = getCode(tempBegin, parallelepiped);
                } else {
                    tempEnd.x = vector.x;
                    tempEnd.y = vector.y;
                    tempEnd.z = vector.z;
                    outCodeEnd = getCode(tempEnd, parallelepiped);
                }
            }
        }
    }

    private int getCode(Vector vector, Parallelepiped parallelepiped) {
        int code = 0;
        if (vector.y > parallelepiped.max.y) {//точка выше области отсечения
            code |= TOP;
        } else if (vector.y < parallelepiped.min.y) {//точка ниже области отсечения
            code |= BOTTOM;
        }
        if (vector.x > parallelepiped.max.x) {//точка правее области отсечения
            code |= RIGHT;
        } else if (vector.x < parallelepiped.min.x) {//точка левее области отсечения
            code |= LEFT;
        }
        if (vector.z > parallelepiped.max.z) {//точка перед областью отсечения
            code |= FRONT;
        } else if (vector.z < parallelepiped.min.z) {//точка за областью отсечения
            code |= BACK;
        }
        return code;
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
