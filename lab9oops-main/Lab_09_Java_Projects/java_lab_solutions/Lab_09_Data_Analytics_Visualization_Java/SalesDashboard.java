import java.io.*;
import java.util.*;

/** Lab 09, Project 2: sales EDA dashboard.
 * CSV columns: date,region,product,customer,sales,profit
 */
public class SalesDashboard {
    static class Sale {
        String date,region,product,customer; double sales,profit;
        Sale(String[] p) {
            date=p[0].trim(); region=p[1].trim(); product=p[2].trim(); customer=p[3].trim();
            sales=number(p[4]); profit=number(p[5]);
        }
    }
    static double number(String s) {
        try { return Double.parseDouble(s.trim()); } catch(Exception e) { return 0.0; }
    }
    static String month(String date) { return date.length()>=7?date.substring(0,7):"Unknown"; }
    static void chart(String title, Map<String,Double> map) {
        System.out.println("\\n"+title);
        double max=map.values().stream().mapToDouble(v->v).max().orElse(1);
        map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> {
            int bars=(int)Math.round(30*Math.max(0,e.getValue())/Math.max(1,max));
            System.out.printf("%-18s %12.2f %s%n",e.getKey(),e.getValue(),"#".repeat(bars));
        });
    }
    public static void main(String[] args) throws Exception {
        String file=args.length>0?args[0]:"sales_data.csv";
        List<Sale> rows=new ArrayList<>();
        try(BufferedReader br=new BufferedReader(new FileReader(file))) {
            String line; boolean first=true;
            while((line=br.readLine())!=null) {
                if(first){first=false; if(line.toLowerCase().startsWith("date,")) continue;}
                String[] p=line.split(",",-1);
                if(p.length<6 || p[0].isBlank() || p[1].isBlank() || p[2].isBlank()) continue;
                rows.add(new Sale(p));
            }
        }
        if(rows.isEmpty()){System.out.println("No valid sales rows. Check CSV path and columns.");return;}
        double totalSales=0,totalProfit=0; Map<String,Double> monthly=new TreeMap<>(),regions=new TreeMap<>(),products=new TreeMap<>(),customers=new TreeMap<>();
        for(Sale s:rows) {
            totalSales+=s.sales; totalProfit+=s.profit;
            monthly.merge(month(s.date),s.sales,Double::sum);
            regions.merge(s.region,s.sales,Double::sum);
            products.merge(s.product,s.sales,Double::sum);
            customers.merge(s.customer,s.sales,Double::sum);
        }
        System.out.println("\\n============= SALES ANALYSIS DASHBOARD =============");
        System.out.println("Valid records: "+rows.size()+" | Unique regions: "+regions.size()+" | Products: "+products.size()+" | Customers: "+customers.size());
        System.out.printf("Total sales: %.2f | Total profit: %.2f | Profit margin: %.2f%%%n",totalSales,totalProfit,totalSales==0?0:100*totalProfit/totalSales);
        chart("Monthly sales",monthly);
        chart("Regional sales",regions);
        chart("Product performance (sales)",products);
        System.out.println("\\nTop 5 customers by sales:");
        customers.entrySet().stream().sorted((a,b)->Double.compare(b.getValue(),a.getValue())).limit(5)
            .forEach(e->System.out.printf("%-20s %.2f%n",e.getKey(),e.getValue()));
        System.out.println("\\nEDA note: invalid/missing numeric fields are treated as zero; review data quality before business decisions.");
    }
}
