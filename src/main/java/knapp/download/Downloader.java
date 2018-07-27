package knapp.download;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.io.IOException;

public class Downloader {

    // Request URL: https://fred.stlouisfed.org/graph/fredgraph.csv?chart_type=line&recession_bars=on&log_scales=&bgcolor=%23e1e9f0&graph_bgcolor=%23ffffff&fo=Open+Sans&ts=12&tts=12&txtcolor=%23444444&show_legend=yes&show_axis_titles=yes&drp=0&cosd=2013-07-14&coed=2018-07-13&height=450&stacking=&range=Custom&mode=fred&id=DEXUSEU&transformation=lin&nd=1999-01-04&ost=-99999&oet=99999&lsv=&lev=&mma=0&fml=a&fgst=lin&fgsnd=2009-06-01&fq=Daily&fam=avg&vintage_date=&revision_date=&line_color=%234572a7&line_style=solid&lw=2&scale=left&mark_type=none&mw=2&width=1168

    private static final String FRED_HOST = "fred.stlouisfed.org";
    private static final String FRED_FORMAT = "https://%s/graph/fredgraph.csv?id=%s&cosd=%s&coed=%s&fq=%s";

    public static String DownloadSeries(DownloadRequest downloadRequest) throws IOException {
        String urlString = String.format(FRED_FORMAT, FRED_HOST,downloadRequest.getSeries(),downloadRequest.getStart().toString(),
                downloadRequest.getEnd().toString(),downloadRequest.getFrequency().name());
        return DownloadToString(urlString);
    }

    public static String DownloadToString(String url) throws IOException {
        HttpClient client = HttpClients.createDefault();
        HttpUriRequest request = new HttpGet(url);
        HttpResponse response = null;
        String text = null;
        try {
            response = client.execute(request);
            text = IOUtils.toString(response.getEntity().getContent());
        } finally {
            response.getEntity().getContent().close();
        }
        return text;
    }

    public static void DownloadToFile(DownloadRequest downloadRequest, File file) throws IOException {
        String text = DownloadSeries(downloadRequest);
        FileUtils.writeStringToFile(file,text);
    }

}
