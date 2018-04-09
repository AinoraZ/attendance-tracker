package Graphics;

import SqliteManipulation.DBManipulator;
import org.jdatepicker.impl.JDatePickerImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.sql.SQLException;


public class MainWindow extends Window {
    JComboBox groups = new JComboBox();
    JComboBox students = new JComboBox();
    DBManipulator manipulator = new DBManipulator();
    JDatePickerImpl date = generateDatePicker();
    JDatePickerImpl start = generateDatePicker();
    JDatePickerImpl end = generateDatePicker();
    GroupEditor editorOfGroups;
    AttendanceEditor editorOfAttendance = null;
    WindowPrinter printerToWindow;

    public MainWindow() {
        redraw();
        editorOfGroups = new GroupEditor(this, manipulator);
    }

    private JPanel generateGroupEdit() {
        JPanel groupEdit = new JPanel(new GridLayout(0,1));

        MainWindow _this = this;
        JButton insert_data = new JButton("Edit Group Data");

        insert_data.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editorOfGroups.showUI();
                editorOfGroups.redraw();
            }
        });

        groupEdit.add(insert_data);
        return groupEdit;
    }

    private JPanel generateAttendanceEdit() {
        JPanel attendanceEdit = new JPanel(new GridLayout(0, 3));
        MainWindow _this = this;

        /* Labels */
        JLabel label = new JLabel("Date");
        attendanceEdit.add(label);
        label = new JLabel("Group");
        attendanceEdit.add(label);
        label = new JLabel("Student");
        attendanceEdit.add(label);
        /* Labels end */

        ResultSet set = manipulator.getAllGroups();
        try{
            boolean first = true;
            while(set.next()){
                String toAdd = set.getString("group_string");
                if(first){
                    putStudents(toAdd);
                    first = false;
                }
                groups.addItem(toAdd);
            }
            set.close();
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }

        groups.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JComboBox combo = (JComboBox) e.getSource();
                        String currentGroup = (String) combo.getSelectedItem();

                        students.removeAllItems();
                        putStudents(currentGroup);
                    }
                }
        );

        JButton insert_data = new JButton("Mark Attendance");
        insert_data.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ComboItem item = (ComboItem) students.getSelectedItem();
                if(item != null && groups.getSelectedItem() != null)
                    setNewAttendanceEditor(_this,
                                            date.getJFormattedTextField().getText(),
                                            groups.getSelectedItem().toString(),
                                            item.getValue(),
                                            manipulator
                                          );
            }
        });

        attendanceEdit.add(date);
        attendanceEdit.add(groups);
        attendanceEdit.add(students);
        attendanceEdit.add(insert_data);

        return attendanceEdit;
    }

    private void setNewAttendanceEditor(MainWindow parent, String date, String group, int student, DBManipulator manipulator){
        if(editorOfAttendance != null)
            editorOfAttendance.dispose();
        editorOfAttendance = new AttendanceEditor(this, date, group, student, manipulator);
    }

    private void putStudents(String group){
        if(group == null)
            return;
        try{
            ResultSet temp_set = manipulator.getAllStudents(group);
            students.addItem(new ComboItem("--ALL STUDENTS--", -1));
            while(temp_set.next()){
                students.addItem(new ComboItem(temp_set.getString("name"), temp_set.getInt("id")));
            }
            temp_set.close();
        }
        catch(SQLException ex){
            System.out.println(ex.getMessage());
        }
    }

    private JPanel generatePrinting() {
        JPanel printing = new JPanel(new GridLayout(0, 2));
        MainWindow _this = this;

        JLabel label = new JLabel("Start Date");
        printing.add(label);
        label = new JLabel("End Date");
        printing.add(label);
        //printing.add(new JLabel());
        //printing.add(new JLabel());

        JButton get_list = new JButton("Get List");
        get_list.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new WindowPrinter(_this, start.getJFormattedTextField().getText(), end.getJFormattedTextField().getText(), manipulator);
            }
        });

        printing.add(start);
        printing.add(end);
        printing.add(get_list);

        return printing;
    }

    public void showUI() {
        this.add(mainPane);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();

        this.setSize(560, 460);

        this.setVisible(true);
        this.setTitle("Attendance program");
    }

    public void redraw(){
        this.getContentPane().removeAll();

        students.removeAllItems();
        groups.removeAllItems();

        reApply();

        JPanel groupEdit = generateGroupEdit();
        JPanel attendanceEdit = generateAttendanceEdit();
        JPanel printing = generatePrinting();

        startPane.add(new JLabel("Student/Group editing"));
        middlePane.add(groupEdit);
        //middlePane.add(new JSeparator(SwingConstants.HORIZONTAL));
        middlePane.add(new JLabel("Attendance Marking"));
        middlePane.add(attendanceEdit);
        middlePane.add(new JLabel("Listing options"));
        middlePane.add(printing);

        this.add(mainPane);

        this.revalidate();
        this.repaint();

        this.setSize(560, 460);
    }

    public static void main(String[] args){
        MainWindow window = new MainWindow();
        window.showUI();
    }

    class ComboItem{
        private String key;
        private int value;

        public ComboItem(String key, int value){
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return key;
        }

        public String getKey() {
            return key;
        }

        public int getValue() {
            return value;
        }
    }
}

