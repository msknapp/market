package knapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import knapp.history.Frequency;
import knapp.table.Table;
import knapp.table.TableImpl;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class IEXRetriever {
    // https://iextrading.com/trading/eligible-symbols/

    private static final String IEX_ENDPOINT = "https://api.iextrading.com/1.0";

    public static void main(String[] args) {
        IEXRetriever iexRetriever = new IEXRetriever();
//        double p = iexRetriever.getPrice("aapl");
//        System.out.println();
        iexRetriever.getChart("aapl",ChartLength.THREEMONTHS);
    }

    public enum ChartLength {
        FIVEYEARS ("5y"),
        TWOYEARS ("2y"),
        ONEYEAR ("1y"),
        SIXMONTHS ("6m"),
        THREEMONTHS ("3m"),
        ONEDAY ("1d");

        private String text;

        private ChartLength(String text) {
            this.text = text;
        }
        public String getText() {
            return text;
        }
    }

    public Table getChart(String symbol, ChartLength chartLength) {
        try {
            symbol = URLEncoder.encode(symbol,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String path = "/stock/"+symbol+"/chart/"+chartLength.getText();
        path += "?filter=date,close";
        String out = get(path,true);
        ObjectMapper objectMapper = new ObjectMapper();
        TableImpl.TableBuilder builder = TableImpl.newBuilder().frequency(Frequency.Daily)
                .column("Adj Close");

        try {
            List<Object> l = objectMapper.readValue(out,List.class);
            for (Object o : l) {
                Map<String,Object> m = (Map<String, Object>) o;
                String ds = (String) m.get("date");
                Number cs = (Number) m.get("close");
                LocalDate d = LocalDate.parse(ds);
                double[] values = new double[]{cs.doubleValue()};
                builder.addRow(d,values);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Table t = builder.build();
        t.setName(symbol);
        return t;
    }

    public double getPrice(String symbol) {
        try {
            symbol = URLEncoder.encode(symbol,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String path = "/stock/"+symbol+"/price";
        String out = get(path,false);
        return Double.parseDouble(out);
    }

    public String get(String path, boolean json) {
        String url = IEX_ENDPOINT+path;
        HttpClient client = HttpClients.createDefault();
        HttpUriRequest request = new HttpGet(url);
        if (json) {
            request.addHeader("Accept","application/json");
        }
        HttpResponse response = null;
        String text = null;
        try {
            response = client.execute(request);
            text = IOUtils.toString(response.getEntity().getContent());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                IOUtils.closeQuietly(response.getEntity().getContent());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return text;
    }
}
