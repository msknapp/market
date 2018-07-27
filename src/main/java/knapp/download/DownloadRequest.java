package knapp.download;

import knapp.history.Frequency;

import java.time.LocalDate;

public class DownloadRequest {

    private String series;
    private LocalDate start;
    private LocalDate end;
    private Frequency frequency;

    public DownloadRequest() {

    }

    public DownloadRequest series(String x) {
        this.series = x;
        return this;
    }

    public DownloadRequest start(String x) {
        this.start = LocalDate.parse(x);
        return this;
    }

    public DownloadRequest end(String x) {
        this.end = LocalDate.parse(x);
        return this;
    }

    public DownloadRequest frequency(String x) {
        this.frequency = Frequency.valueOf(x);
        return this;
    }

    public DownloadRequest start(LocalDate x) {
        this.start = x;
        return this;
    }

    public DownloadRequest end(LocalDate x) {
        this.end = x;
        return this;
    }

    public DownloadRequest frequency(Frequency x) {
        this.frequency = x;
        return this;
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
