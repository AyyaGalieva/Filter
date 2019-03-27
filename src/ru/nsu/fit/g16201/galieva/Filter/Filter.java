package ru.nsu.fit.g16201.galieva.Filter;

import ru.nsu.fit.g16201.galieva.Filter.Model.Model;
import ru.nsu.fit.g16201.galieva.Filter.View.GUI;

public class Filter {
    public static void main(String[] args) {
        Model m = new Model();
        GUI view = new GUI(m);
        m.setView(view);
        view.setVisible(true);
    }
}
