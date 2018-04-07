package Graphics;

import org.jdatepicker.impl.JDatePickerImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;



public class MainWindow extends Window {
    JComboBox groups = new JComboBox();
    public MainWindow(){

    }

    public void generateWindow(){
        JPanel groupEdit = generateGroupEdit();
        JPanel attendanceEdit = generateAttendanceEdit();

    }

    private JPanel generateGroupEdit(){
        JPanel groupEdit = new JPanel(new GridLayout(0, 1));
        MainWindow _this = this;
        JButton insert_data = new JButton("Group Data");
        insert_data.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new GroupEditor(_this);
            }
        });

        groupEdit.add(insert_data);
        return groupEdit;
    }

    private JPanel generateAttendanceEdit(){
        JPanel attendanceEdit = new JPanel(new GridLayout(0, 2));
        MainWindow _this = this;

        JDatePickerImpl date = generateDatePicker();

        JButton insert_data = new JButton("Mark Attendance");
        insert_data.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AttendanceEditor(_this, date.getJFormattedTextField().getText());
            }
        });

        JComboBox students = new JComboBox();

        groups.addActionListener(
                new ActionListener(){
                    public void actionPerformed(ActionEvent e){
                        /*
                        Check for non empty date
                         */

                        JComboBox combo = (JComboBox) e.getSource();
                        String currentGroup = (String) combo.getSelectedItem();

                        /*
                        Insert JSON code for currentGroup
                        Update available student temp_list
                        student = new JComboBox(temp_list);
                         */
                    }
                }
        );

        attendanceEdit.add(date);
        attendanceEdit.add(groups);
        attendanceEdit.add(students);
        attendanceEdit.add(insert_data);

        return attendanceEdit;
    }

    public void redraw(){

    }
}

