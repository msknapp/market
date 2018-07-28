package knapp.indicator;

import knapp.download.DownloadRequest;
import knapp.history.Frequency;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Indicator {

    private final String series;
    private final LocalDate start;
    private final Frequency frequency;
    private final String description;

    public Indicator(String series,LocalDate start,Frequency frequency,String description){
        this.series = series;
        this.start = start;
        this.frequency = frequency;
        this.description = description;
    }

    public static List<Indicator> toIndicators(List<String> series, LocalDate retrievalStart) {
        List<Indicator> indicators = new ArrayList<>();
        for (String s : series) {
            Indicator indicator = new Indicator(s,retrievalStart,Frequency.Monthly,"whatever");
            indicators.add(indicator);
        }
        return indicators;
    }

    public String getSeries() {
        return series;
    }

    public LocalDate getStart() {
        return start;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public String getDescription() {
        return description;
    }

    public DownloadRequest toDownloadRequest() {
        return new DownloadRequest().frequency(frequency).start(start).end(LocalDate.now()).series(series);
    }

    public static List<Indicator> parseFromText(String text, boolean hasHeader) {
        boolean first = true;
        List<Indicator> indicators = new ArrayList<>();
        for (String line : text.split("\n")) {
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            if (first && hasHeader) {
                first = false;
                continue;
            }
            String[] parts = line.split(",");
            String series = parts[0];
            LocalDate start = LocalDate.parse(parts[1]);
            Frequency frequency = Frequency.valueOf(parts[2]);
            String description = parts[3];
            indicators.add(new Indicator(series,start,frequency,description));
        }
        return indicators;
    }
}
