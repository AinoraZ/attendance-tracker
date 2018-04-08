package Graphics;

import javax.swing.*;
import java.awt.*;

public class GroupEditor extends Window{
    public GroupEditor(MainWindow parent){

    }

    public void showUI() {
        this.add(mainPane);

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.pack();

        Rectangle r = this.getBounds();
        this.setSize(r.width + 20, r.height);

        this.setVisible(true);
    }

    public void redraw(){

    }
}
