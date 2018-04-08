package Graphics;

import SqliteManipulation.DBManipulator;
import javax.swing.*;
import java.awt.*;

public class AttendanceEditor extends Window{
    public AttendanceEditor(MainWindow parent, String date, String group, int student, DBManipulator manipulator){
        if(date.equals(""))
            return;

        System.out.println("Date: " + date);
        System.out.println("Student: " + student);
        System.out.println("Group: " + group);
    }

    public void showUI() {
        this.add(mainPane);

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.pack();

        Rectangle r = this.getBounds();
        this.setSize(r.width + 20, r.height);

        this.setVisible(true);
    }

    public void redraw(){

    }

}
