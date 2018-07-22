package knapp.download;

import java.time.LocalDate;

public class DownloadRequest {

    public enum Frequency {
        Daily,
        Monthly,
        Quarterly,
        Annual
    }

    private String series;
    private LocalDate start;
    private LocalDate end;
    private Frequency frequency;

    public DownloadRequest() {

    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public LocalDate getStart() {
        return start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }
}
