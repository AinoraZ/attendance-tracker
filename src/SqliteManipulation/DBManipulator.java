package SqliteManipulation;
import org.jetbrains.annotations.Nullable;

import javax.xml.transform.Result;
import java.sql.*;
import java.text.ParseException;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/*
SQLITE:
Students - _id, name, group
attendance - _id, date, group_id, student_id, visited

If we need to check all visited lectures by certain students, Select * from Lectures where student_id = {student_id}
If we need to check all visited lectures by a group, Select * from Lectures where group_id = {group_id}
If a student is deleted, Delete from Students where _id = {student_id}, Delete from Lectures where student_id = {student_id}
If we need certain date and certain student, Select * from Lectures where student_id = {student_id} and date = {date}
If we need certain date and certain group, Select * from Lectures where group_id = {group_id} and date = {date}
 */


public class DBManipulator {
    Connection attendance;
    DatabaseMetaData attendance_meta;

    /**
     * Creates or opens the proper .sqlite file and establishes connection
     * <p>
     * Creates the necessary tables if they do not exist
     */
    public DBManipulator(){
        connectToDatabase();

        String students_sql = "CREATE TABLE IF NOT EXISTS students (\n"
                + " id integer PRIMARY KEY, \n"
                + " name text NOT NULL, \n"
                + " group_id integer \n"
                + ");";
        createNewTable(students_sql);

        String lectures_sql = "CREATE TABLE IF NOT EXISTS attendance (\n"
                + " id integer PRIMARY KEY, \n"
                + " date text NOT NULL, \n"
                + " group_id integer NOT NULL, \n"
                + " student_id integer NOT NULL, \n"
                + " attend integer NOT NULL \n"
                + ");";
        createNewTable(lectures_sql);

        String groups_sql = "CREATE TABLE IF NOT EXISTS groups (\n"
                + " id integer PRIMARY KEY, \n"
                + " group_string text UNIQUE \n"
                + ");";
        createNewTable(groups_sql);

    }

    @Nullable
    private void connectToDatabase() {
        String url = "jdbc:sqlite:" + "attendance.sqlite";

        try{
            Connection conn = DriverManager.getConnection(url);
            attendance = conn;
            if(attendance != null)
                attendance_meta = attendance.getMetaData();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private boolean tableExists(String tableName){
        try{
            ResultSet rs = attendance_meta.getTables(null, null, tableName, null);
            rs.last();
            return rs.getRow() > 0;
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return false;
    }

    private void createNewTable(String sql_command){
        try{
            Statement stmt = attendance.createStatement();
            stmt.execute(sql_command);
        } catch (SQLException e) {
            System.out.println("Oh no");
            System.out.println(e.getMessage());
        }
    }

    /* DEPRICATED */
    private List<ResultSet> getMultipleStudents(List<Integer> ids){
        if(ids.size() == 0)
            return null;

        List<ResultSet> rs = new ArrayList<>();
        for(int id: ids){
            ResultSet temp_set = getStudent(id);
            rs.add(temp_set);
            try {
                System.out.println(rs.get(0).next());
            }
            catch(SQLException e){

            }
        }
        return rs;
    }

    private int getGroupId(String group){
        String sql = "SELECT id FROM groups WHERE group_string = ?";

        try{
            PreparedStatement stmt  = attendance.prepareStatement(sql);
            stmt.setString(1, group);
            ResultSet rs = stmt.executeQuery();

            if(rs.next())
                return rs.getInt("id");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }

    /**
     * Gets all of the available groups
     * @return ResultSet of all the groups
     */
    public ResultSet getAllGroups(){
        String sql = "SELECT group_string FROM groups";

        try{
            Statement stmt  = attendance.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            return rs;
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * Gets all of the students belonging to a group
     * @param group - Unique String identifier of the group
     * @return ResultSet of all the students in that group
     */
    public ResultSet getAllStudents(String group){
        String sql = "SELECT * FROM students WHERE group_id = ?";
        int group_id = getGroupId(group);
        if(group_id == -1)
            return null;

        try{
            PreparedStatement stmt  = attendance.prepareStatement(sql);
            stmt.setInt(1, group_id);
            ResultSet rs = stmt.executeQuery();
            return rs;
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * Gets one student by it's Primary key (Unique ID)
     * @param id - Primary key of a student
     * @return ResultSet containing a single student
     */
    public ResultSet getStudent(int id){
        String sql = "SELECT * FROM students WHERE id = ?";
        try{
            PreparedStatement stmt  = attendance.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs;
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * Get all students from a group who either attended or did not attend on a certain date
     * @param group Unique String value of the group
     * @param date String value yyyy-mm-dd
     * @param attend boolean value of desired attendance
     * @return ResultSet of all students who attended or not on a certain date
     */
    public List<Integer> getGroupAttend(String group, String date, boolean attend){
        String sql = "SELECT student_id FROM attendance WHERE date = ? AND attend = ? AND group_id = ?";

        int group_id = getGroupId(group);
        if(group_id == -1)
            return null;

        try{
            PreparedStatement stmt  = attendance.prepareStatement(sql);
            stmt.setString(1, date);
            stmt.setInt(2, attend? 1 : 0);
            stmt.setInt(3, group_id);
            List<Integer> sets = new ArrayList<>();
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                int student_id = rs.getInt("student_id");
                sets.add(student_id);
            }
            return sets;
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * Returns the attendance of a single student.
     * @param date String in format yyyy-mm-dd
     * @param student_id Primary key of the student
     * @return int value. 1 is returned if attended, 0 if not attended, -1 if record does not exist.
     */
    public int getStudentOnDate(String date, int student_id){
        String sql = "SELECT attend FROM attendance WHERE date = ? AND student_id = ?";

        try{
            PreparedStatement stmt  = attendance.prepareStatement(sql);
            stmt.setString(1, date);
            stmt.setInt(2, student_id);
            ResultSet rs = stmt.executeQuery();
            return rs.getInt("attend");
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }


    /**
     * Insert new group
     * @param group - Unique string identifier of a group
     */
    public void insertToGroups(String group){
        String sql = "INSERT INTO groups(group_string) VALUES(?)";

        try{
            PreparedStatement preped = attendance.prepareStatement(sql);
            preped.setString(1, group);
            preped.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Insert new student
     * @param name the name of the student
     * @param group the group the student belongs to
     */
    public void insertToStudents(String name, String group){
        String sql = "INSERT INTO students(name,group_id) VALUES(?,?)";

        int group_id = getGroupId(group);
        if(group_id == -1)
            return;

        try{
            PreparedStatement preped = attendance.prepareStatement(sql);
            preped.setString(1, name);
            preped.setInt(2, group_id);
            preped.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Insert new attendance of a student on a certain date
     * @param date String of the date the student attended or not format YYYY-MM-DD
     * @param group String unique identifier of the group of the student
     * @param student_id int the Primary key of a student
     * @param attend whether the student attended or not
     */
    public void insertToAttendance(String date, String group, int student_id, boolean attend){
        String sql = "INSERT INTO attendance(date,group_id,student_id,attend) VALUES(?,?,?,?)";

        int group_id = getGroupId(group);
        if(group_id == -1)
            return;

        try{
            PreparedStatement preped = attendance.prepareStatement(sql);
            preped.setString(1, date);
            preped.setInt(2, group_id);
            preped.setInt(3, student_id);
            preped.setInt(4, attend? 1: 0);
            preped.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Deletes selected group, all of the students belonging to that group, and all of the records of those students
     * @param group String unique identifier that should be deleted
     */
    public void deleteGroup(String group){
        String sql = "DELETE FROM groups WHERE id = ?";

        int group_id = getGroupId(group);
        if(group_id == -1)
            return;

        try{
            /*ResultSet students = getAllStudents(group);
            while(students.next()){
                deleteStudent(students.getInt("id"));
            }*/
            PreparedStatement preped = attendance.prepareStatement(sql);
            preped.setInt(1, group_id);
            preped.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        sql = "DELETE FROM students WHERE group_id = ?";

        try{
            PreparedStatement preped = attendance.prepareStatement(sql);
            preped.setInt(1, group_id);
            preped.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        sql = "DELETE FROM attendance WHERE group_id = ?";

        try{
            PreparedStatement preped = attendance.prepareStatement(sql);
            preped.setInt(1, group_id);
            preped.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Deletes selected student and all their records
     * @param student_id int Primary key of a student
     */
    public void deleteStudent(int student_id){
        String sql = "DELETE FROM students WHERE id = ?";

        try{
            PreparedStatement preped = attendance.prepareStatement(sql);
            deleteAttendance(student_id);
            preped.setInt(1, student_id);
            preped.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void deleteAttendance(int student_id){
        String sql = "DELETE FROM attendance WHERE student_id = ?";

        try{
            PreparedStatement preped = attendance.prepareStatement(sql);
            preped.setInt(1, student_id);
            preped.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Updates the unique identifier of a selected group
     * @param old_group old String unique identifier of a group
     * @param group new String unique identifier of a group
     */
    public void updateGroups(String old_group, String group){
        String sql = "UPDATE groups SET group_string = ? WHERE id = ?";

        int group_id = getGroupId(old_group);
        if(group_id == -1)
            return;

        try {
            PreparedStatement preped = attendance.prepareStatement(sql);
            preped.setString(1, group);
            preped.setInt(2, group_id);
            preped.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Updates the selected students name and group
     * @param student_id int Primary key of a student
     * @param name The new name of a student
     * @param group The new group of a student
     */
    public void updateStudents(int student_id, String name, String group){
        String sql = "UPDATE students SET name = ?, group_id = ? WHERE id = ?";

        int group_id = getGroupId(group);
        if(group_id == -1)
            return;

        try{
            PreparedStatement preped = attendance.prepareStatement(sql);
            preped.setString(1, name);
            preped.setInt(2, group_id);
            preped.setInt(3, student_id);
            preped.executeUpdate();
            updateAttendance(student_id, group);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Updates the attendance of a student on a certain date
     * @param update_student_id the int Primary Key of student whose attendance is updated (Part of Composite Key)
     * @param update_date the String of date to be updated format YYYY-MM-DD (Part of Composite Key)
     * @param attend boolean whether the student attended or not
     */
    public void updateAttendance(int update_student_id, String update_date, boolean attend){
        String sql = "UPDATE attendance SET attend = ? WHERE student_id = ? AND date = ?";


        try{
            PreparedStatement preped = attendance.prepareStatement(sql);
            preped.setInt(1, attend? 1: 0);
            preped.setInt(2, update_student_id);
            preped.setString(3, update_date);

            preped.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Updates the group of every attendance record of a student
     * @param update_student_id the int Primary Key of student whose group is updated
     * @param group the unique String identifier of a group
     */
    public void updateAttendance(int update_student_id, String group){
        String sql = "UPDATE attendance SET group_id = ? WHERE student_id = ?";

        int group_id = getGroupId(group);
        if(group_id == -1)
            return;

        try{
            PreparedStatement preped = attendance.prepareStatement(sql);
            preped.setInt(1, group_id);
            preped.setInt(2, update_student_id);

            preped.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /*
    public void updateAttendance(int update_student_id, String update_date, String date, String group, int student_id, boolean attend){
        String sql = "UPDATE groups SET date = ?, group_id = ?, student_id = ?, attend = ? WHERE student_id = ? AND date = ?";

        int group_id = getGroupId(group);
        if(group_id == -1)
            return;

        try(PreparedStatement preped = attendance.prepareStatement(sql)) {
            preped.setString(1, date);
            preped.setInt(2, group_id);
            preped.setInt(3, student_id);
            preped.setInt(4, attend? 1: 0);
            preped.setInt(5, student_id);
            preped.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    */

    public static void _test() {
        DBManipulator manipulator = new DBManipulator();
        /* Group insert */
        manipulator.insertToGroups("group_1");
        manipulator.insertToGroups("group_2");

        /* Group get */
        ResultSet set = manipulator.getAllGroups();

        List<String> groups = new ArrayList<>();
        try {
            while (set.next()) {
                groups.add(set.getString("group_string"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        /* Student insert (tested) */
        manipulator.insertToStudents("Ainoras Zukauskas", groups.get(0));
        //manipulator.insertToStudents("Ainoras Zukauskas", groups.get(0));
        //manipulator.insertToStudents("Ainoras Zukauskas", groups.get(0));
        //manipulator.insertToStudents("Lukas Rimavicius", groups.get(1));

        /* All student get */
        set = manipulator.getAllStudents(groups.get(1));

        try {
            while (set.next()) {
                System.out.print(set.getString("name"));
                System.out.println(set.getInt("group_id"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        /* Single student get */
        set = manipulator.getStudent(1);

        try {
            while (set.next()) {
                System.out.print(set.getString("name"));
                System.out.println(set.getInt("group_id"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        if(manipulator.getStudentOnDate("2018-04-12", 1) == -1)
            manipulator.insertToAttendance("2018-04-12", groups.get(0), 1, false);
        int attended = manipulator.getStudentOnDate("2018-04-12", 1);
        System.out.println("Attended " + attended + " Expected: 0");

        if(manipulator.getStudentOnDate("2018-04-12", 2) == -1)
            manipulator.insertToAttendance("2018-04-12", groups.get(0), 2, true);
        if(manipulator.getStudentOnDate("2018-04-12", 3) == -1)
            manipulator.insertToAttendance("2018-04-12", groups.get(0), 3, true);

        List<Integer> id_set = manipulator.getGroupAttend(groups.get(0), "2018-04-12", true);

        try{
            for(int temp_id: id_set){
                set = manipulator.getStudent(temp_id);
                while (set.next()){
                    System.out.print(set.getString("id"));
                    System.out.print(" " + set.getString("name"));
                    System.out.println(" " + set.getInt("group_id"));
                }
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        manipulator.updateGroups("group_1", "group_1");
        manipulator.updateStudents(1, "Lukas Glavinskas", "group_1");
        manipulator.updateAttendance(1, "2018-04-12", false);
    }
}
