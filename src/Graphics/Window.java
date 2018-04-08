package Graphics;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

/**
 * Window class responsible for the main program GUI interface window
 *
 * @author Ainoras Žukauskas
 * @version 2018-03-19
 */

public abstract class Window extends JFrame implements WindowInterface{
    JPanel mainPane = new JPanel(new BorderLayout());
    JPanel labelPane = new JPanel(new GridLayout(0, 1));
    JPanel fieldPane = new JPanel(new GridLayout(0, 1));
    JPanel endPane = new JPanel(new GridLayout(0, 2));

    /**
     * Sets up the main JPanel for the window instance
     */
    public Window(){
        mainPane.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        mainPane.add(labelPane, BorderLayout.CENTER);
        mainPane.add(fieldPane, BorderLayout.LINE_END);
        mainPane.add(endPane, BorderLayout.PAGE_END);
    }

    public JDatePickerImpl generateDatePicker(){
        UtilDateModel model = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());

        return datePicker;
    }
}


class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {

    private String datePattern = "yyyy-MM-dd";
    private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

    @Override
    public Object stringToValue(String text) throws ParseException {
        return dateFormatter.parseObject(text);
    }

    @Override
    public String valueToString(Object value) throws ParseException {
        if (value != null) {
            Calendar cal = (Calendar) value;
            return dateFormatter.format(cal.getTime());
        }

        return "";
    }

}