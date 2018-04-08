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

public class GroupEditor extends Window{
    JScrollPane scrollPane = new JScrollPane();
    JPanel resultPane = null;
    JPanel groupAdder;
    DBManipulator manipulator;
    MainWindow parent;

    public GroupEditor(MainWindow parent, DBManipulator manipulator){
        this.parent = parent;
        this.manipulator = manipulator;
        showUI();
        redraw();
    }

    public void addGroups(){
        groupAdder = new JPanel(new GridLayout(0, 2));

        JTextPane group_name = new JTextPane(new DefaultStyledDocument(){
            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                if ((getLength() + str.length()) < 36 && !str.equals("\n")) {
                    super.insertString(offs, str, a);
                }
            }
        });
        groupAdder.add(group_name);
        groupAdder.add(addGroupButton(group_name));
    }

    public void groupResults(){
        resultPane = new JPanel(new GridLayout(0, 3));
        scrollPane = new JScrollPane(resultPane);
        //scrollPane = new JScrollPane(resultPane);

        try {
            ResultSet set = manipulator.getAllGroups();
            while(set.next()){
                String group_string = set.getString("group_string");
                resultPane.add(new JLabel(group_string));
                //resultPane.add(new JLabel());
                resultPane.add(new JLabel());
                resultPane.add(deleteButton(group_string));

                JTextPane student_name = new JTextPane(new DefaultStyledDocument(){
                    @Override
                    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                        if ((getLength() + str.length()) < 28 && !str.equals("\n")) {
                            super.insertString(offs, str, a);
                        }
                    }
                });

                resultPane.add(new JLabel("Add student"));
                resultPane.add(student_name);
                //resultPane.add(new JLabel());
                resultPane.add(addStudentButton(student_name, group_string));

                ResultSet students = manipulator.getAllStudents(group_string);
                while(students.next()) {
                    int student_id = students.getInt("id");
                    resultPane.add(new JLabel());
                    //resultPane.add(new JLabel(Integer.toString(student_id)));
                    resultPane.add(new JLabel(students.getString("name")));
                    resultPane.add(deleteStudent(student_id));
                }
            }
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }

    }

    private JButton addGroupButton(JTextPane group_name){
        JButton delete = new JButton("ADD group");
        GroupEditor _this = this;

        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(group_name.getText().length() != 0) {
                    manipulator.insertToGroups(group_name.getText());
                    redraw();
                }
            }
        });

        return delete;
    }

    private JButton deleteButton(String group){
        JButton delete = new JButton("DEL Group");
        GroupEditor _this = this;

        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                manipulator.deleteGroup(group);
                redraw();
            }
        });

        return delete;
    }

    private JButton deleteStudent(int id){
        JButton delete = new JButton("DEL Student");
        GroupEditor _this = this;

        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                manipulator.deleteStudent(id);
                redraw();
            }
        });

        return delete;
    }

    private JButton addStudentButton(JTextPane name, String group_string){
        JButton delete = new JButton("ADD Student");
        GroupEditor _this = this;

        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                manipulator.insertToStudents(name.getText(), group_string);
                redraw();
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
    }

    public void redraw(){
        int policy = scrollPane.getVerticalScrollBar().getValue();
        this.remove(mainPane);
        mainPane = new JPanel(new BorderLayout());
        mainPane.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        addGroups();
        mainPane.add(groupAdder, BorderLayout.PAGE_START);
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
