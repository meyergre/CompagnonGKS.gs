package fr.lepetitpingouin.android.gks;

import android.util.Log;

/**
 * Created by meyergre on 31/05/13.
 */
public class BSize {

    private String rawData;
    private String unit;
    private String tmp[];
    private double koctets;

    public BSize(String input) {
        try {
            this.rawData = input.replaceAll(",", ".");
            tmp = rawData.split(" ");
            this.unit = tmp[tmp.length - 1];
            double raw = Double.valueOf(tmp[tmp.length - 2]);

            koctets = unit.contains("ko") ? raw : unit.contains("Mo") ? raw * (1024) : unit.contains("Go") ? raw * (1024 * 1024) : unit.contains("To") ? raw * (1024 * 1024 * 1024) : raw / 1024;
        } catch (Exception e) {
            this.unit = "ko";
            this.koctets = 0;
        }
    }

    public String convert() {
        return convert(this.unit);

    }

    public String convert(String unit) {
        return unit.contains("ko") ? String.format("%.2f", getInko()) + " Ko" : unit.contains("Mo") ? String.format("%.2f", getInMo()) + " Mo" : unit.contains("Go") ? String.format("%.2f", getInGo()) + " Go" : unit.contains("To" +
                "") ? String.format("%.2f", getInTo()) + " To" : String.format("%.2f", getInoctets()) + " o";
    }

    public double getInBaseUnit() {
        Log.d("getInBaseUnit()", String.valueOf(tmp[tmp.length - 2]));
        return Double.valueOf(tmp[tmp.length - 2]);
    }

    public String getBaseUnit() {
        return unit;
    }

    public double getInoctets() {
        return koctets * 1024;
    }

    public double getInko() {
        return koctets;
    }

    public double getInMo() {
        return koctets / (1024);
    }

    public double getInGo() {
        return koctets / (1024 * 1024);
    }

    public double getInTo() {
        return koctets / (1024 * 1024 * 1024);
    }
}
