package knapp;

import knapp.download.DownloadRequest;
import knapp.history.Frequency;
import knapp.indicator.Indicator;
import knapp.table.DefaultGetMethod;
import knapp.table.Table;
import knapp.table.TableImpl;
import knapp.table.TableParser;
import knapp.util.CurrentDirectory;
import knapp.util.Util;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;

import static knapp.download.Downloader.DownloadSeries;
import static knapp.util.Util.doWithDate;

public class DataRetriever {

    private final BiFunction<Table,Integer,TableImpl.GetMethod> getMethodChooser;

    public DataRetriever() {
        this(new DefaultGetMethod());
    }

    public DataRetriever(BiFunction<Table,Integer,TableImpl.GetMethod> getMethodChooser) {
        this.getMethodChooser = getMethodChooser;
    }

    public Map<String,Table> retrieveData(List<Indicator> indicators) throws IOException {
        LocalDate start = LocalDate.of(1979,1,1);
        LocalDate end = LocalDate.of(2019,1,1);
        return retrieveData(start, end, indicators);
    }

    public Map<String,Table> retrieveData(LocalDate start, LocalDate end, List<Indicator> indicators) throws IOException {
        boolean first = true;
        Map<String,Table> downloadedData = new TreeMap<String,Table>();
        for (Indicator indicator : indicators) {
            if ("NASDAQCOM".equals(indicator.getSeries()) || "SP500".equals(indicator.getSeries())) {
                // these don't give me accurate data from FRED.
                continue;
            }
            DownloadRequest downloadRequest = indicator.toDownloadRequest();
            String data = DownloadSeries(downloadRequest);
            Table table = TableParser.parse(data,true,indicator.getFrequency());
            table.setName(indicator.getSeries());
            downloadedData.put(indicator.getSeries(), table);
        }

        return downloadedData;
    }

    public void writeData(LocalDate start, LocalDate end, Map<String,Table> downloadedData,
                          Table market, CurrentDirectory currentDirectory, String destinationFile) {

        Frequency frequency = Frequency.Monthly;

        Util.ExceptionalConsumer<Writer> consumer = writer -> {
            writer.write("Date");
            for (String key : downloadedData.keySet()) {
                writer.write(",");
                writer.write(key);
            }
            writer.write("\n");
            final TableImpl.GetMethod marketMethod = getMethodChooser.apply(market,1);

            doWithDate(start,end,Frequency.Monthly, d -> {
                writer.write(d.toString());
                for (String key : downloadedData.keySet()) {
                    writer.write(",");
                    Table table = downloadedData.get(key);
                    TableImpl.GetMethod method = getMethodChooser.apply(table,0);
                    double value = table.getValue(d, 0, method);
                    String sValue = String.valueOf(value);
                    if ("Infinity".equals(sValue)) {
                        throw new IllegalArgumentException("got an infinity (not like the car).");
                    }
                    writer.write(sValue);
                }
                writer.write("\n");
            });
        };
        String text = Util.doWithWriter(consumer);
        File file = currentDirectory.toFile(destinationFile);
        try {
            FileUtils.writeStringToFile(file, text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
