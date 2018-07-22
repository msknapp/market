package knapp;

import knapp.download.DownloadRequest;
import knapp.history.Frequency;
import knapp.indicator.Indicator;
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
import java.util.function.Function;

import static knapp.download.Downloader.DownloadSeries;
import static knapp.util.Util.doWithDate;

public class DataRetriever {

    private final TableImpl market;
    private final CurrentDirectory currentDirectory;
    private final Function<TableImpl,TableImpl.GetMethod> getMethodChooser;

    public DataRetriever(TableImpl market, CurrentDirectory currentDirectory,
                         Function<TableImpl,TableImpl.GetMethod> getMethodChooser) {
        if (market == null) {
            throw new IllegalArgumentException("market data can't be null");
        }
        this.market = market;
        this.getMethodChooser = getMethodChooser;
        this.currentDirectory = currentDirectory;
    }

    public DataRetriever(TableImpl market, CurrentDirectory currentDirectory) {
        this(market,currentDirectory, tableImpl -> {
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
        Map<String,TableImpl> downloadedData = new TreeMap<String,TableImpl>();
        for (Indicator indicator : indicators) {
            if ("NASDAQCOM".equals(indicator.getSeries()) || "SP500".equals(indicator.getSeries())) {
                // these don't give me accurate data from FRED.
                continue;
            }
            DownloadRequest downloadRequest = indicator.toDownloadRequest();
            String data = DownloadSeries(downloadRequest);
            TableImpl tableImpl = TableParser.parse(data,true);
            downloadedData.put(indicator.getSeries(), tableImpl);
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
            final TableImpl.GetMethod marketMethod = getMethodChooser.apply(market);

            doWithDate(start,end,Frequency.Monthly, d -> {
                try {
                    writer.write(d.toString());
                    for (String key : downloadedData.keySet()) {
                        writer.write(",");
                        TableImpl tableImpl = downloadedData.get(key);
                        TableImpl.GetMethod method = getMethodChooser.apply(tableImpl);
                        double value = tableImpl.getValue(d, 1, method);
                        writer.write(String.valueOf(value));
                    }
                    writer.write(",");
                    double value = market.getValue(d, 5, marketMethod);
                    writer.write(String.valueOf(value));
                    writer.write("\n");
                }catch (Exception e) {

                }
            });
        };
        Util.writeToFile(consumer,outFile);
    }
}
