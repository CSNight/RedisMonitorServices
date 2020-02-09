package csnight.redis.monitor.redis.statistic;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.db.elastic.ElasticRestClientAPI;
import csnight.redis.monitor.db.jpa.*;
import csnight.redis.monitor.utils.BaseUtils;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ElasticRmsLogExecutorImpl implements RmsLogsExecutor {
    private Logger _log = LoggerFactory.getLogger(ElasticRmsLogExecutorImpl.class);
    private boolean isAccessible = true;
    private ElasticRestClientAPI restAPI;

    public ElasticRmsLogExecutorImpl(String es_addresses) {
        JSONObject es_mapping = GetEsConfig();
        if (es_addresses != null && es_mapping != null) {
            restAPI = new ElasticRestClientAPI(es_addresses, es_mapping);
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
        if (!restAPI.SetIndices()) {
            isAccessible = false;
            _log.error("Elasticsearch redis statistic log executors initialize failed because set indices failed");
            return;
        }
        if (restAPI.getBulk() == null) {
            isAccessible = false;
            _log.error("Elasticsearch redis statistic log executors initialize failed because bulk processor create failed");
            return;
        }
        _log.info("Elasticsearch redis statistic log executors initialize success");
    }

    public static JSONObject GetEsConfig() {
        try {
            return JSONObject.parseObject(ReadFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String ReadFile() {
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        try {
            InputStream fileInputStream;
            ClassPathResource resource = new ClassPathResource("classpath:es_mapping.json");
            if (!resource.exists()) {
                fileInputStream = new FileInputStream(BaseUtils.getResourceDir() + "es_mapping.json");
            } else {
                fileInputStream = resource.getInputStream();
            }
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            reader = new BufferedReader(inputStreamReader);
            String tempString;
            while ((tempString = reader.readLine()) != null) {
                content.append(tempString);
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
        return content.toString();
    }

    @Override
    public boolean isAccessible() {
        return isAccessible;
    }

    @Override
    public void checkAccess() {
        isAccessible = restAPI.isConnected() && restAPI.getBulk() != null;
    }

    @Override
    public boolean execute(List<RmsLog> logs) {
        BulkProcessor bulkProcessor = restAPI.getBulk();
        if (bulkProcessor != null) {
            for (RmsLog log : logs) {
                switch (log.getSector()) {
                    case "Physical":
                        IndexRequest pRequest = new IndexRequest("rps_log");
                        pRequest.source(restAPI.GenerateDocByPojo(JSONObject.toJSONString((RmsRpsLog)log), "rps_log"));
                        bulkProcessor.add(pRequest);
                        break;
                    case "Commands":
                        IndexRequest oRequest = new IndexRequest("ros_log");
                        oRequest.source(restAPI.GenerateDocByPojo(JSONObject.toJSONString((RmsRosLog)log), "ros_log"));
                        bulkProcessor.add(oRequest);
                        break;
                    case "Clients":
                        IndexRequest cRequest = new IndexRequest("rcs_log");
                        cRequest.source(restAPI.GenerateDocByPojo(JSONObject.toJSONString((RmsRcsLog)log), "rcs_log"));
                        bulkProcessor.add(cRequest);
                        break;
                    case "Keyspace":
                        IndexRequest kRequest = new IndexRequest("rks_log");
                        kRequest.source(restAPI.GenerateDocByPojo(JSONObject.toJSONString((RmsRksLog)log), "rks_log"));
                        bulkProcessor.add(kRequest);
                        break;
                }
            }
        } else {
            isAccessible = false;
        }
        return false;
    }

    @Override
    public boolean destroy() {
        return restAPI.CloseES();
    }
}
