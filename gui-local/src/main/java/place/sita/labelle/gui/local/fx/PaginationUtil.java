package place.sita.labelle.gui.local.fx;

public class PaginationUtil {

    public static int getNumberOfPages(int count, int pageSize) {
        if (count % pageSize == 0) {
            return (count/pageSize);
        } else {
            return count/pageSize + 1;
        }
    }

}
