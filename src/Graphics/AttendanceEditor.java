package Graphics;

import SqliteManipulation.DBManipulator;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class AttendanceEditor extends Window{
    MainWindow parent;
    String date;
    String group;
    int student_id;
    DBManipulator manipulator;

    JScrollPane scrollPane = new JScrollPane();
    JPanel studentsPanel;
    JPanel prepPanel;

    boolean allStudents = true;


    public AttendanceEditor(MainWindow parent, String date, String group, int student, DBManipulator manipulator){
        if(date.equals(""))
            return;

        this.parent = parent;
        this.student_id = student;
        this.group = group;
        this.date = date;
        this.manipulator = manipulator;

        if(this.student_id != -1)
            allStudents = false;

        System.out.println("Date: " + date);
        System.out.println("Student: " + student);
        System.out.println("Group: " + group);

        prepStart();
        startPane.add(prepPanel);

        if(allStudents)
            showAllStudents();
        else
            showSingleStudent();
        showUI();
    }

    private void prepStart(){
        prepPanel = new JPanel(new GridLayout(0, 2));
        prepPanel.add(new JLabel(date));

        AttendanceEditor _this = this;
        JButton delete = new JButton("DEL date");

        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(allStudents)
                    manipulator.deleteAttendance(date, group);
                else
                    manipulator.deleteAttendance(date, student_id);
                _this.dispose();
            }
        });

        prepPanel.add(delete);
    }

    private void showAllStudents(){
        studentsPanel = new JPanel(new GridLayout(0 ,2));
        scrollPane = new JScrollPane(studentsPanel);

        List<Integer> attended = manipulator.getGroupAttend(group, date, true);
        List<Integer> notAttended = manipulator.getGroupAttend(group, date, false);
        JCheckBox checker;

        try {
            ResultSet students = manipulator.getAllStudents(group);
            while(students.next()){
                int temp_id = students.getInt("id");
                studentsPanel.add(new JLabel(students.getString("name")));
                checker = new JCheckBox("Attended");

                if(attended.contains(temp_id))
                    checker.setSelected(true);
                else if(notAttended.contains(temp_id))
                    checker.setSelected(false);
                else {
                    manipulator.insertToAttendance(date, group, temp_id, false);
                    checker.setSelected(false);
                }

                checker.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        manipulator.updateAttendance(temp_id, date, e.getStateChange() == 1);
                    }
                });

                studentsPanel.add(checker);
            }
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }

    }

    private void showSingleStudent(){
        studentsPanel = new JPanel(new GridLayout(0 ,2));
        scrollPane = new JScrollPane(studentsPanel);

        int attended = manipulator.getStudentOnDate(date, student_id);
        JCheckBox checker;

        try {
            ResultSet students = manipulator.getStudent(student_id);
            while(students.next()){
                int temp_id = students.getInt("id");
                studentsPanel.add(new JLabel(students.getString("name")));
                checker = new JCheckBox("Attended");

                if(attended == 1)
                    checker.setSelected(true);
                else if(attended == 0)
                    checker.setSelected(false);
                else{
                    manipulator.insertToAttendance(date, group, temp_id, false);
                    checker.setSelected(false);
                }

                checker.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        manipulator.updateAttendance(temp_id, date, e.getStateChange() == 1);
                    }
                });

                studentsPanel.add(checker);
            }
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }

    }

    public void showUI() {
        this.add(mainPane);
        mainPane.add(scrollPane);

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.pack();

        Rectangle r = this.getBounds();
        if(r.height < 500)
            this.setSize(600, r.height);
        else
            this.setSize(600, 500);

        this.setVisible(true);
    }

    public void redraw(){

    }

}
