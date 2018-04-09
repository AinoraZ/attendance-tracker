package Graphics;

import SqliteManipulation.DBManipulator;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class WindowPrinter extends Window {
    MainWindow parent;
    String startDate;
    String endDate;
    DBManipulator manipulator;
    JScrollPane scrollPane;
    JTextArea printingArea;
    PdfPTable table = new PdfPTable(new float[] { 2, 2, 1 });

    public WindowPrinter(MainWindow parent, String startDate, String endDate, DBManipulator manipulator){
        if(startDate.equals("") || endDate.equals(""))
            return;

        this.parent = parent;
        this.startDate = startDate;
        this.endDate = endDate;
        this.manipulator = manipulator;
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);

        generatePrinting();
        showUI();
    }

    public void generatePrinting(){
        printingArea = new JTextArea("");
        printingArea.setEditable(false);
        scrollPane = new JScrollPane(printingArea);
        int tableIndex = 0;
        try {
            /* Getting single dat a*/
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date convertedStart = sdf.parse(startDate);
            java.util.Date convertedEnd = sdf.parse(endDate);

            Calendar cal = Calendar.getInstance();
            cal.setTime(convertedStart);
            while (cal.getTime().before(convertedEnd) || cal.getTime().equals(convertedEnd)) {
                String formattedDate = sdf.format(cal.getTime());
                cal.add(Calendar.DATE, 1);

                /* Got the single date PRINTING*/
                boolean singleDate = false;

                ResultSet allGroups = manipulator.getAllGroups();
                try{
                    while(allGroups.next()){
                        String group_string = allGroups.getString("group_string");
                        /* Got single group string, PRINTING */
                        boolean singleGroup = false;

                        List<Integer> attended = manipulator.getGroupAttend(group_string, formattedDate, true);
                        List<Integer> notAttended = manipulator.getGroupAttend(group_string, formattedDate, false);

                        ResultSet students = manipulator.getAllStudents(group_string);
                        while(students.next()){
                            int temp_id = students.getInt("id");

                            /* Print student of group, if on that date */

                            if(attended.contains(temp_id)) {
                                if(!singleDate){
                                    printingArea.append(formattedDate + "\n");
                                    table.addCell(formattedDate);
                                    table.addCell("");
                                    table.addCell("");

                                    PdfPCell[] cells = table.getRow(tableIndex).getCells();
                                    for (int j=0;j<cells.length;j++){
                                        cells[j].setBackgroundColor(BaseColor.GRAY);
                                    }
                                    tableIndex++;

                                    singleDate = true;
                                }
                                if(!singleGroup){
                                    printingArea.append("\t" + group_string + "\n");
                                    table.addCell(group_string);
                                    table.addCell("");
                                    table.addCell("");

                                    PdfPCell[] cells = table.getRow(tableIndex).getCells();
                                    for (int j=0;j<cells.length;j++){
                                        cells[j].setBackgroundColor(BaseColor.LIGHT_GRAY);
                                    }
                                    tableIndex++;

                                    singleGroup = true;
                                }
                                String temp_student = students.getString("name");
                                printingArea.append("\t\t" + temp_student + " Attended\n");
                                table.addCell("");
                                table.addCell(temp_student);
                                table.addCell("Attended");
                                tableIndex++;
                            }
                            else if(notAttended.contains(temp_id)) {
                                if(!singleDate){
                                    printingArea.append(formattedDate + "\n");
                                    table.addCell(formattedDate);
                                    table.addCell("");
                                    table.addCell("");
                                    singleDate = true;

                                    PdfPCell[] cells = table.getRow(tableIndex).getCells();
                                    for (int j=0;j<cells.length;j++){
                                        cells[j].setBackgroundColor(BaseColor.GRAY);
                                    }
                                    tableIndex++;
                                }
                                if(!singleGroup){
                                    printingArea.append("\t" + group_string + "\n");
                                    table.addCell(group_string);
                                    table.addCell("");
                                    table.addCell("");
                                    singleGroup = true;

                                    PdfPCell[] cells = table.getRow(tableIndex).getCells();
                                    for (int j=0;j<cells.length;j++){
                                        cells[j].setBackgroundColor(BaseColor.LIGHT_GRAY);
                                    }
                                    tableIndex++;
                                }
                                String temp_student = students.getString("name");
                                printingArea.append("\t\t" + temp_student + " Not Attended\n");
                                table.addCell("");
                                table.addCell(temp_student);
                                table.addCell("Not Attended");

                                tableIndex++;
                            }
                        }
                    }
                }
                catch (SQLException e){
                    System.out.println(e.getMessage());
                }
            }
        }
        catch (ParseException e){
            System.out.println(e.getMessage());
        }
    }

    public void generatePdf(){
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream("attendance.pdf"));
            document.open();
            //document.add(new Paragraph(text));
            document.add(table);
            document.close();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            JOptionPane.showMessageDialog(null, "Failed to create PDF", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(null, "Created pdf", "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void showUI(){
        JButton print_list = new JButton("Print List");
        print_list.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generatePdf();
            }
        });

        startPane.add(print_list);

        mainPane.add(scrollPane);
        this.add(mainPane);

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(560, 460);

        this.setVisible(true);
        this.setTitle("List Print");

    }

    public void redraw(){

    }
}
