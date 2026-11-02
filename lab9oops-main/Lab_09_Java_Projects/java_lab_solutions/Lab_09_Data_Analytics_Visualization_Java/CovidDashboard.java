import java.io.*;
import java.util.*;

/** Lab 09, Project 1: COVID-19 dataset analysis and console dashboard.
 * CSV columns: date,country,confirmed,recovered,deaths,vaccinated
 */
public class CovidDashboard {
    static class Row {
        String date,country; long confirmed,recovered,deaths,vaccinated;
        Row(String[] p) {
            date=p[0].trim(); country=p[1].trim();
            confirmed=number(p[2]); recovered=number(p[3]); deaths=number(p[4]); vaccinated=number(p[5]);
        }
    }
    static long number(String s) {
        try { return Math.max(0, Long.parseLong(s.trim())); }
        catch(Exception e) { return 0; } // invalid/missing numeric values treated as zero
    }
    public static void main(String[] args) throws Exception {
        String file=args.length>0?args[0]:"covid_data.csv";
        List<Row> rows=new ArrayList<>();
        try(BufferedReader br=new BufferedReader(new FileReader(file))) {
            String line; boolean first=true;
            while((line=br.readLine())!=null) {
                if(first){first=false; if(line.toLowerCase().startsWith("date,")) continue;}
                String[] p=line.split(",",-1); if(p.length<6 || p[0].isBlank() || p[1].isBlank()) continue;
                rows.add(new Row(p));
            }
        }
        if(rows.isEmpty()){System.out.println("No valid data rows. Check CSV path and columns.");return;}
        Map<String,Row> latestByCountry=new TreeMap<>();
        Map<String,long[]> byDate=new TreeMap<>();
        for(Row r:rows) {
            Row prev=latestByCountry.get(r.country);
            if(prev==null || r.date.compareTo(prev.date)>0) latestByCountry.put(r.country,r);
            long[] v=byDate.computeIfAbsent(r.date,k->new long[4]);
            v[0]+=r.confirmed; v[1]+=r.recovered; v[2]+=r.deaths; v[3]+=r.vaccinated;
        }
        long cases=0,recovered=0,deaths=0,vaccinated=0;
        for(Row r:latestByCountry.values()){cases+=r.confirmed; recovered+=r.recovered; deaths+=r.deaths; vaccinated+=r.vaccinated;}
        System.out.println("\\n========== COVID-19 ANALYSIS DASHBOARD ==========");
        System.out.println("Valid records: "+rows.size()+" | Countries: "+latestByCountry.size()+" | Dates: "+byDate.size());
        System.out.printf("Latest-per-country totals: confirmed %,d | recovered %,d | deaths %,d | vaccinated %,d%n",cases,recovered,deaths,vaccinated);
        System.out.println("\\nCountry-wise confirmed cases (latest date in each country):");
        latestByCountry.values().stream().sorted((a,b)->Long.compare(b.confirmed,a.confirmed)).limit(10)
            .forEach(r->System.out.printf("%-20s %,12d%n",r.country,r.confirmed));
        System.out.println("\\nTrend by date (summed rows; # scaled relative to maximum):");
        long max=byDate.values().stream().mapToLong(v->v[0]).max().orElse(1);
        for(Map.Entry<String,long[]> e:byDate.entrySet()) {
            int bars=(int)Math.round(35.0*e.getValue()[0]/Math.max(1,max));
            System.out.printf("%-12s %,10d %s%n",e.getKey(),e.getValue()[0],"#".repeat(bars));
        }
        System.out.println("\\nNote: totals depend on the supplied data's definition and coverage; this sample is illustrative.");
    }
}
