import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.gl2.GLUT;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;

public class Clipping implements GLEventListener {

    private final FloatBuffer cubeColor = FloatBuffer.wrap(new float[]{1.0F, 1.0F, 0.0F});//цвет отсекающего полигона
    private final FloatBuffer clippedSectionColor = FloatBuffer.wrap(new float[]{1.0F, 1.0F, 1.0F});//цвет отсеченной части отрезка
    private final FloatBuffer unclippedSectionColor = FloatBuffer.wrap(new float[]{1.0F, 1.0F, 1.0F});//цвет не отсеченной части отрезка

    private final List<Vector> sectionVectors = Arrays.asList(
            new Vector((float) (Math.random() * 25 - 12.5), (float) (Math.random() * 25 - 12.5), (float) (Math.random() * 25 - 12.5)),
            new Vector((float) (Math.random() * 25 - 12.5), (float) (Math.random() * 25 - 12.5), (float) (Math.random() * 25 - 12.5)),
            new Vector((float) (Math.random() * 25 - 12.5), (float) (Math.random() * 25 - 12.5), (float) (Math.random() * 25 - 12.5))
    );//массив векторов отсекаемых отрезков
    private GL2 gl;
    private GLU glu;
    private GLUT glut;

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
        gl = drawable.getGL().getGL2();
        glu = new GLU();
        glut = new GLUT();

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

        glu.gluLookAt(10, 15, 10, 0d, 0d, 0d, 0d, 0.5d, 0d);

        //отрисовка отсекающего куба
        gl.glColor3fv(cubeColor);
        gl.glPushMatrix();
        gl.glTranslated(0, 0, 0);
        glut.glutWireCube(5);
        gl.glPopMatrix();

        gl.glBegin(GL2.GL_LINES);
        gl.glColor3fv(clippedSectionColor);
        for (int j = 0; j < sectionVectors.size(); j ++) {
            //вектор начала ОТРЕЗКА
            Vector sectionVectorA = sectionVectors.get(j);
            //отрисовка ОТРЕЗКА
            gl.glVertex3f(sectionVectorA.x, sectionVectorA.y, sectionVectorA.z);
            gl.glVertex3f(-sectionVectorA.x, -sectionVectorA.y, -sectionVectorA.z);
        }
        gl.glEnd();
    }


    public void dispose(GLAutoDrawable glAutoDrawable) {
    }
}
