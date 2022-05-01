import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class Clipping implements GLEventListener {

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
        //добавление слушателя GL - инстанса самого класса
        Clipping clipping = new Clipping();
        canvas.addGLEventListener(clipping);
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
            gl.glVertex3f(edgeVectorA.x, edgeVectorA.y, edgeVectorA.z);
            gl.glVertex3f(edgeVectorB.x, edgeVectorB.y, edgeVectorB.z);
        }
        gl.glColor3fv(clippedSectionColor);
        for (int j = 0; j < sectionVectors.size() - 1; j += 2) {
            //вектор начала ОТРЕЗКА
            Vector sectionVectorA = sectionVectors.get(j);
            //вектор конца ОТРЕЗКА
            Vector sectionVectorB = sectionVectors.get(j + 1);
            //отрисовка ОТРЕЗКА
            gl.glVertex3f(sectionVectorA.x, sectionVectorA.y, sectionVectorA.z);
            gl.glVertex3f(sectionVectorB.x, sectionVectorB.y, sectionVectorB.z);
        }
        gl.glColor3fv(unclippedSectionColor);
        for (int i = 0; i < polygonVectors.size(); i++) {
        }
        gl.glEnd();
    }


    public void dispose(GLAutoDrawable glAutoDrawable) {
    }
}
