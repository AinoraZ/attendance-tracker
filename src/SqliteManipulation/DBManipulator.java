package SqliteManipulation;
import org.jetbrains.annotations.Nullable;

import javax.xml.transform.Result;
import java.sql.*;
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
                + " group_id integer, \n"
                + " UNIQUE(id) \n"
                + ");";
        createNewTable(students_sql);

        String lectures_sql = "CREATE TABLE IF NOT EXISTS attendance (\n"
                + " id integer PRIMARY KEY, \n"
                + " date text NOT NULL, \n"
                + " group_id integer NOT NULL, \n"
                + " student_id integer NOT NULL, \n"
                + " attend integer NOT NULL, \n"
                + " UNIQUE(id)"
                + ");";
        createNewTable(lectures_sql);

        String groups_sql = "CREATE TABLE IF NOT EXISTS groups (\n"
                + " id integer PRIMARY KEY, \n"
                + " group text NOT NULL, \n"
                + " UNIQUE(id, group)"
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
     * Gets all of the available groups
     * @return ResultSet of all the groups
     */
    public ResultSet getAllGroups(){
        String sql = "SELECT group FROM group";

        try (Statement stmt  = attendance.createStatement()){
            stmt.execute(sql);
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
        String sql = "SELECT student_id FROM attendance WHERE date = ? AND attend = ? AND group = ?";

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

    /**
     * Returns the attendance of a single student.
     * @param date String in format yyyy-mm-dd
     * @param student_id Primary key of the student
     * @return int value. 1 is returned if attended, 0 if not attended, -1 if record does not exist.
     */
    public int getStudentOnDate(String date, int student_id){
        String sql = "SELECT attend FROM attendance WHERE date = ? AND student_id = ?";

        try (PreparedStatement stmt  = attendance.prepareStatement(sql)){
            stmt.setString(1, date);
            ResultSet rs = stmt.executeQuery(sql);
            return rs.getInt("attend");
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }

    public ResultSet getAllGroupAttend(String group, String start, String end, boolean attend){
        String sql = "SELECT student_id FROM attendance WHERE attend = ? AND group = ? AND date BETWEEN ? AND ?";

        int group_id = getGroupId(group);
        if(group_id == -1)
            return null;

        try (PreparedStatement stmt  = attendance.prepareStatement(sql)){
            stmt.setInt(1, attend? 1 : 0);
            stmt.setInt(2, group_id);
            stmt.setString(3, start);
            stmt.setString(4, end);
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

    /**
     * Insert new group
     * @param group - Unique string
     */
    public void insertToGroups(String group){
        String sql = "INSERT INTO groups(group) VALUES(?)";

        try(PreparedStatement preped = attendance.prepareStatement(sql)) {
            preped.setString(1, group);
            preped.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
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

    /* attendance - id, date, group_id, student_id, attend */
    public void insertToAttendance(String date, String group, int student_id, boolean attend){
        String sql = "INSERT INTO attendance(date,group_id,student_id,attend) VALUES(?,?,?,?)";

        int group_id = getGroupId(group);
        if(group_id == -1)
            return;

        try(PreparedStatement preped = attendance.prepareStatement(sql)) {
            preped.setString(1, date);
            preped.setInt(2, group_id);
            preped.setInt(3, student_id);
            preped.setInt(4, attend? 1: 0);
            preped.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void deleteGroup(String group){
        String sql = "DELETE FROM groups WHERE id = ?";

        int group_id = getGroupId(group);
        if(group_id == -1)
            return;

        try(PreparedStatement preped = attendance.prepareStatement(sql)){
            ResultSet students = getAllStudents(group);
            while(students.next()){
                deleteStudent(students.getInt("id"));
            }
            preped.setInt(1, group_id);
            preped.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void deleteStudent(int student_id){
        String sql = "DELETE FROM students WHERE id = ?";

        try(PreparedStatement preped = attendance.prepareStatement(sql)){
            deleteAttendance(student_id);
            preped.setInt(1, student_id);
            preped.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void deleteAttendance(int student_id){
        String sql = "DELETE FROM attendance WHERE student_id = ?";

        try(PreparedStatement preped = attendance.prepareStatement(sql)){
            preped.setInt(1, student_id);
            preped.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
