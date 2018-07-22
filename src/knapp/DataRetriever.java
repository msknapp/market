package knapp;

import knapp.download.DownloadRequest;
import knapp.history.Frequency;
import knapp.indicator.Indicator;
import knapp.table.Table;
import knapp.table.TableImpl;
import knapp.table.TableParser;
import knapp.util.CurrentDirectory;
import knapp.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import static knapp.download.Downloader.DownloadSeries;
import static knapp.util.Util.doWithDate;

public class DataRetriever {

    private final Table market;
    private final CurrentDirectory currentDirectory;
    private final BiFunction<Table,Integer,TableImpl.GetMethod> getMethodChooser;

    public DataRetriever(Table market, CurrentDirectory currentDirectory,
                         BiFunction<Table,Integer,TableImpl.GetMethod> getMethodChooser) {
        if (market == null) {
            throw new IllegalArgumentException("market data can't be null");
        }
        this.market = market;
        this.getMethodChooser = getMethodChooser;
        this.currentDirectory = currentDirectory;
    }

    public DataRetriever(TableImpl market, CurrentDirectory currentDirectory) {
        this(market,currentDirectory, (tableImpl, col) -> {
            return TableImpl.GetMethod.LAST_KNOWN_VALUE;
        });
    }

    public void consolidateData() throws IOException {
        consolidateData("temp-output.csv");
    }

    public void consolidateData(String destinationFile) throws IOException {
        String text = currentDirectory.toText("current-indicators.csv");
        List<Indicator> indicators = Indicator.parseFromText(text, true);
        consolidateData(indicators, destinationFile);
    }

    public void consolidateData(List<Indicator> indicators, String destinationFile) throws IOException {
        LocalDate start = LocalDate.of(1979,1,1);
        LocalDate end = LocalDate.of(2019,1,1);
        consolidateData(start, end, indicators, destinationFile);
    }

    public void consolidateData(LocalDate start, LocalDate end, List<Indicator> indicators,
                                String destinationFile) throws IOException {
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
            downloadedData.put(indicator.getSeries(), table);
        }

        Frequency frequency = Frequency.Monthly;

        File outFile = currentDirectory.toFile(destinationFile);
        Util.ExceptionalConsumer<Writer> consumer = writer -> {
            writer.write("Date");
            for (String key : downloadedData.keySet()) {
                writer.write(",");
                writer.write(key);
            }
            writer.write(",");
            String marketName = market.getName();
            if (marketName == null || marketName.isEmpty()) {
                marketName = "Market Price";
            }
            writer.write(marketName);
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
                writer.write(",");
                double value = market.getValue(d, 5, marketMethod);
                writer.write(String.valueOf(value));
                writer.write("\n");
            });
        };
        Util.writeToFile(consumer,outFile);
    }
}
