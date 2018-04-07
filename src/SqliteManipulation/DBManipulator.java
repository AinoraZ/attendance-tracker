package SqliteManipulation;
import org.jetbrains.annotations.Nullable;

import javax.xml.transform.Result;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
Json data:
Group data - (group_id): {(student_id):name}]
Attendance data - (date) : {(group_id):[{student_id:(student_id), attendance: (true, false)}]}

SQLITE:
Students - _id, name, group
Lectures - _id, date, group_id, student_id, visited

If we need to check all visited lectures by certain students, Select * from Lectures where student_id = {student_id}
If we need to check all visited lectures by a group, Select * from Lectures where group_id = {group_id}
If a student is deleted, Delete from Students where _id = {student_id}, Delete from Lectures where student_id = {student_id}
If we need certain date and certain student, Select * from Lectures where student_id = {student_id} and date = {date}
If we need certain date and certain group, Select * from Lectures where group_id = {group_id} and date = {date}
 */


public class DBManipulator {
    Connection attendance;
    DatabaseMetaData attendance_meta;

    public DBManipulator(){
        attendance = createNewDatabase("attendance.sqlite");
        try{
            if(attendance != null)
                attendance_meta = attendance.getMetaData();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        String students_sql = "CREATE TABLE IF NOT EXISTS students (\n"
                + " id integer PRIMARY KEY, \n"
                + " name text NOT NULL, \n"
                + " group_id integer\n"
                + ");";
        createNewTable(students_sql);

        String lectures_sql = "CREATE TABLE IF NOT EXISTS lectures (\n"
                + " id integer PRIMARY KEY, \n"
                + " date text NOT NULL, \n"
                + " group_id integer NOT NULL, \n"
                + " student_id integer NOT NULL, \n"
                + " visited integer"
                + ");";
        createNewTable(lectures_sql);

        String groups_sql = "CREATE TABLE IF NOT EXISTS lectures (\n"
                + " id integer PRIMARY KEY, \n"
                + " group text NOT NULL, \n"
                + ");";
        createNewTable(groups_sql);

    }

    @Nullable
    private static Connection createNewDatabase(String fileName) {
        String url = "jdbc:sqlite:../" + fileName;

        try (Connection conn = DriverManager.getConnection(url)) {
            return conn;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
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
            System.out.println(e.getMessage());
        }
    }

    private ResultSet getMultipleStudents(List<Integer> id){
        String sql = "SELECT * FROM students WHERE id IN ?";

        try (PreparedStatement stmt  = attendance.prepareStatement(sql)){
            Integer[] data = id.toArray(new Integer[id.size()]);
            java.sql.Array sqlArray = attendance.createArrayOf("integer", data);
            stmt.setArray(1, sqlArray);
            ResultSet rs = stmt.executeQuery(sql);
            return rs;
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private int getGroupId(String group){
        String sql = "SELECT id FROM groups WHERE group = ?";

        try (PreparedStatement stmt  = attendance.prepareStatement(sql)){
            stmt.setString(1, group);
            ResultSet rs    = stmt.executeQuery(sql);

            // loop through the result set
            while (rs.next()) {
                return rs.getInt("id");
                /*
                System.out.println(rs.getInt("id") +  "\t" +
                        rs.getString("name") + "\t" +
                        rs.getDouble("capacity"));
                */
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
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

        try (PreparedStatement stmt  = attendance.prepareStatement(sql)){
            stmt.setInt(1, group_id);
            ResultSet rs = stmt.executeQuery(sql);
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

        try (PreparedStatement stmt  = attendance.prepareStatement(sql)){
            stmt.setInt(1, id);
            ResultSet rs    = stmt.executeQuery(sql);
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
    public ResultSet getGroupAttend(String group, String date, boolean attend){
        String sql = "SELECT student_id FROM lectures WHERE date = ? AND visited = ? AND group = ?";

        int group_id = getGroupId(group);
        if(group_id == -1)
            return null;

        try (PreparedStatement stmt  = attendance.prepareStatement(sql)){
            stmt.setString(1, date);
            stmt.setInt(2, attend? 1 : 0);
            stmt.setInt(3, group_id);
            List<Integer> sets = new ArrayList<>();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                int student_id = rs.getInt("student_id");
                sets.add(student_id);
            }
            return getMultipleStudents(sets);
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    /*
    Return all students who are registered to lectures on this date
    */
    public ResultSet getAllOnDate(String date){
        String sql = "SELECT student_id FROM lectures WHERE date = ?";

        try (PreparedStatement stmt  = attendance.prepareStatement(sql)){
            stmt.setString(1, date);
            List<Integer> sets = new ArrayList<>();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                int student_id = rs.getInt("student_id");
                sets.add(student_id);
            }
            return getMultipleStudents(sets);
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public void insertToStudents(String name, String group){
        String sql = "INSERT INTO students(name,group_id) VALUES(?,?)";

        int group_id = getGroupId(group);
        if(group_id == -1)
            return;

        try(PreparedStatement preped = attendance.prepareStatement(sql)) {
            preped.setString(1, name);
            preped.setInt(2, group_id);
            preped.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /* Lectures - id, date, group_id, student_id, visited */
    public void insertToLectures(String date, String group, int student_id, boolean visited){
        String sql = "INSERT INTO lectures(date,group_id,student_id,visited) VALUES(?,?,?,?)";

        int group_id = getGroupId(group);
        if(group_id == -1)
            return;

        try(PreparedStatement preped = attendance.prepareStatement(sql)) {
            preped.setString(1, date);
            preped.setInt(2, group_id);
            preped.setInt(3, student_id);
            preped.setInt(4, visited? 1: 0);
            preped.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void insertToGroups(String group){
        String sql = "INSERT INTO groups(group) VALUES(?)";

        try(PreparedStatement preped = attendance.prepareStatement(sql)) {
            preped.setString(1, group);
            preped.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}
