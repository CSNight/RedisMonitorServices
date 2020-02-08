package csnight.redis.monitor.redis.statistic;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.auth.config.RmsLogPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class ElasticRmsLogExecutorImpl implements RmsLogsExecutor {
    private Logger _log = LoggerFactory.getLogger(ElasticRmsLogExecutorImpl.class);
    private boolean isAccessible = true;
    private ElasticRestAPI restAPI;

    public ElasticRmsLogExecutorImpl(String es_addresses) {
        JSONObject es_mapping = GetEsConfig();
        if (es_addresses != null && es_mapping != null) {
            restAPI = new ElasticRestAPI(es_addresses, es_mapping);
        } else {
            isAccessible = false;
            _log.error("Elasticsearch redis statistic log executors initialize failed because config lost");
            return;
        }
        if (!restAPI.isConnected()) {
            isAccessible = false;
            _log.error("Elasticsearch redis statistic log executors initialize failed because connection failed");
            return;
        }
        _log.info("Elasticsearch redis statistic log executors initialize");
    }

    public static JSONObject GetEsConfig() {
        String Path = Objects.requireNonNull(RmsLogPoolConfig.class.getClassLoader().getResource("")).getPath() + "es_mapping.json";
        try {
            return JSONObject.parseObject(ReadFile(Path));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String ReadFile(String Path) {
        BufferedReader reader = null;
        StringBuilder laststr = new StringBuilder();
        try {
            FileInputStream fileInputStream = new FileInputStream(Path);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            reader = new BufferedReader(inputStreamReader);
            String tempString;
            while ((tempString = reader.readLine()) != null) {
                laststr.append(tempString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return laststr.toString();
    }

    @Override
    public boolean isAccessible() {
        return isAccessible;
    }

    @Override
    public void checkAccess() {
        isAccessible = restAPI.isConnected();
    }

    @Override
    public boolean execute(List<RmsLog> logs) {
        logs.forEach(log -> System.out.println("ES--" + JSONObject.toJSON(log)));
        return false;
    }

    @Override
    public boolean destroy() {
        return restAPI.CloseES();
    }
}
