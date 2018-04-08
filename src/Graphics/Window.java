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
    JPanel startPane = new JPanel(new GridLayout(0, 1));
    JPanel middlePane = new JPanel(new GridLayout(0, 1));
    JPanel endPane = new JPanel(new GridLayout(0, 1));

    /**
     * Sets up the main JPanel for the window instance
     */
    public Window(){
        mainPane.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        mainPane.add(startPane, BorderLayout.PAGE_START);
        mainPane.add(middlePane, BorderLayout.CENTER);
        mainPane.add(endPane, BorderLayout.PAGE_END);
    }

    public void reApply(){
        mainPane.remove(startPane);
        mainPane.remove(middlePane);
        mainPane.remove(endPane);

        startPane = new JPanel(new GridLayout(0, 1));
        middlePane = new JPanel(new GridLayout(0, 1));
        endPane = new JPanel(new GridLayout(0, 1));

        mainPane.add(startPane, BorderLayout.PAGE_START);
        mainPane.add(middlePane, BorderLayout.CENTER);
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