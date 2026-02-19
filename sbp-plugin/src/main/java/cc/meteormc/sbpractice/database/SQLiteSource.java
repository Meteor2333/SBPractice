package cc.meteormc.sbpractice.database;

import cc.meteormc.sbpractice.Main;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jdbc.db.SqliteDatabaseType;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class SQLiteSource extends JdbcConnectionSource {
    public SQLiteSource() throws IOException, SQLException {
        File folder = Main.get().getDataFolder();
        folder.mkdirs();
        File file = new File(folder, "data.db");
        if (!file.exists()) {
            file.createNewFile();
        }

        this.setUrl("jdbc:sqlite:" + file);
        this.setDatabaseType(new SqliteDatabaseType());
        this.initialize();
    }
}
