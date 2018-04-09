package Graphics;

import SqliteManipulation.DBManipulator;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentEditor extends Window{
    JScrollPane scrollPane = new JScrollPane();
    JPanel resultPane = null;
    DBManipulator manipulator;
    MainWindow parent;

    public StudentEditor(MainWindow parent, DBManipulator manipulator){
        this.parent = parent;
        this.manipulator = manipulator;
        //showUI();
        //redraw();
    }

    public void groupResults(){
        resultPane = new JPanel(new GridLayout(0, 3));
        scrollPane = new JScrollPane(resultPane);
        //scrollPane = new JScrollPane(resultPane);

        try {
            ResultSet set = manipulator.getAllGroups();
            while(set.next()){
                String group_string = set.getString("group_string");
                JTextPane group_name_pane;
                resultPane.add(new JLabel(group_string));
                resultPane.add(new JLabel(""));
                resultPane.add(new JLabel(""));


                ResultSet students = manipulator.getAllStudents(group_string);
                while(students.next()) {
                    int student_id = students.getInt("id");
                    JTextPane student_name;
                    try {
                        student_name = new JTextPane(new DefaultStyledDocument() {
                            @Override
                            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                                if ((getLength() + str.length()) < 28 && !str.equals("\n")) {
                                    super.insertString(offs, str, a);
                                }
                            }
                        });
                        student_name.getStyledDocument().insertString(0, students.getString("name"), null);

                        ResultSet group_set = manipulator.getAllGroups();
                        int group_index = 0;
                        int temp_index = 0;
                        JComboBox groups = new JComboBox();
                        while(group_set.next()){
                            String toAdd = group_set.getString("group_string");
                            if(toAdd.equals(group_string))
                                group_index = temp_index;
                            groups.addItem(toAdd);
                            temp_index++;
                        }
                        groups.setSelectedIndex(group_index);
                        resultPane.add(student_name);
                        resultPane.add(groups);
                        resultPane.add(updateStudent(student_id, student_name, groups));
                    }
                    catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }

    }

    private JButton updateStudent(int id, JTextPane student_name, JComboBox group_name){
        JButton delete = new JButton("EDIT Student");

        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(group_name.getSelectedItem() != null) {
                    manipulator.updateStudents(id, student_name.getText(), group_name.getSelectedItem().toString());
                    redraw();
                }
            }
        });

        return delete;
    }

    public void showUI() {
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.pack();

        this.setVisible(true);
        this.setResizable(false);

        this.add(mainPane);
        this.setTitle("Student Editor");
    }

    public void redraw(){
        int policy = scrollPane.getVerticalScrollBar().getValue();
        this.remove(mainPane);
        mainPane = new JPanel(new BorderLayout());
        mainPane.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        groupResults();
        mainPane.add(scrollPane, BorderLayout.CENTER);
        this.add(mainPane);

        this.pack();
        Rectangle r = this.getBounds();

        if(r.height < 650)
            this.setSize(800, r.height);
        else
            this.setSize(800, 650);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                scrollPane.getVerticalScrollBar().setValue(policy);
            }
        });

        parent.redraw();
    }
}
