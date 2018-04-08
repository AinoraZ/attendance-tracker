package Graphics;

import org.jdatepicker.impl.JDatePickerImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;



public class MainWindow extends Window {
    JComboBox groups = new JComboBox();

    public MainWindow() {

    }

    public void generateWindow() {
        JPanel groupEdit = generateGroupEdit();
        JPanel attendanceEdit = generateAttendanceEdit();

    }

    private JPanel generateGroupEdit() {
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

    private JPanel generateAttendanceEdit() {
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
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
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

    public void showUI() {
        this.add(mainPane);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();

        Rectangle r = this.getBounds();
        this.setSize(r.width + 20, r.height);

        this.setVisible(true);
    }

    public void redraw(){

    }

    /*
    public List<List<Integer>> getAllGroupAttend(String group, String start, String end, boolean attend){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            java.util.Date convertedStart = sdf.parse(start);
            java.util.Date convertedEnd = sdf.parse(end);

            Calendar cal = Calendar.getInstance();
            cal.setTime(convertedStart);
            List<List<Integer>> list_set = new ArrayList<List<Integer>>();
            while (cal.getTime().before(convertedEnd) || cal.getTime().equals(convertedEnd)) {
                String formatted = sdf.format(cal.getTime());
                cal.add(Calendar.DATE, 1);

                List<Integer> temp_list = getGroupAttend(group, formatted, attend);
                list_set.add(temp_list);
            }

            return list_set;
        }
        catch (ParseException e){
            return null;
        }
    }
    */
}

