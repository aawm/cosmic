package com.cloud.api.query.dao;

import com.cloud.api.ApiDBUtils;
import com.cloud.api.query.vo.StoragePoolJoinVO;
import com.cloud.api.response.StoragePoolResponse;
import com.cloud.capacity.CapacityManager;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.legacymodel.storage.StoragePool;
import com.cloud.legacymodel.storage.StorageStats;
import com.cloud.utils.StringUtils;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StoragePoolJoinDaoImpl extends GenericDaoBase<StoragePoolJoinVO, Long> implements StoragePoolJoinDao {
    public static final Logger s_logger = LoggerFactory.getLogger(StoragePoolJoinDaoImpl.class);
    private final SearchBuilder<StoragePoolJoinVO> spSearch;
    private final SearchBuilder<StoragePoolJoinVO> spIdSearch;
    @Inject
    private ConfigurationDao _configDao;

    protected StoragePoolJoinDaoImpl() {

        spSearch = createSearchBuilder();
        spSearch.and("idIN", spSearch.entity().getId(), SearchCriteria.Op.IN);
        spSearch.done();

        spIdSearch = createSearchBuilder();
        spIdSearch.and("id", spIdSearch.entity().getId(), SearchCriteria.Op.EQ);
        spIdSearch.done();

        _count = "select count(distinct id) from storage_pool_view WHERE ";
    }

    @Override
    public StoragePoolResponse newStoragePoolResponse(final StoragePoolJoinVO pool) {
        final StoragePoolResponse poolResponse = new StoragePoolResponse();
        poolResponse.setId(pool.getUuid());
        poolResponse.setName(pool.getName());
        poolResponse.setState(pool.getStatus());
        String path = pool.getPath();
        //cifs store may contain password entry, remove the password
        path = StringUtils.cleanString(path);
        poolResponse.setPath(path);
        poolResponse.setIpAddress(pool.getHostAddress());
        poolResponse.setZoneId(pool.getZoneUuid());
        poolResponse.setZoneName(pool.getZoneName());
        poolResponse.setType(pool.getPoolType().toString());
        poolResponse.setPodId(pool.getPodUuid());
        poolResponse.setPodName(pool.getPodName());
        poolResponse.setCreated(pool.getCreated());
        if (pool.getScope() != null) {
            poolResponse.setScope(pool.getScope().toString());
        }
        if (pool.getHypervisor() != null) {
            poolResponse.setHypervisor(pool.getHypervisor().toString());
        }

        final long allocatedSize = pool.getUsedCapacity() + pool.getReservedCapacity();
        poolResponse.setDiskSizeTotal(pool.getCapacityBytes());
        poolResponse.setDiskSizeAllocated(allocatedSize);
        poolResponse.setCapacityIops(pool.getCapacityIops());

        // TODO: StatsCollector does not persist data
        final StorageStats stats = ApiDBUtils.getStoragePoolStatistics(pool.getId());
        if (stats != null) {
            final Long used = stats.getByteUsed();
            poolResponse.setDiskSizeUsed(used);
        }

        poolResponse.setClusterId(pool.getClusterUuid());
        poolResponse.setClusterName(pool.getClusterName());
        poolResponse.setTags(pool.getTag());
        poolResponse.setOverProvisionFactor(Double.toString(CapacityManager.StorageOverprovisioningFactor.valueIn(pool.getId())));

        // set async job
        if (pool.getJobId() != null) {
            poolResponse.setJobId(pool.getJobUuid());
            poolResponse.setJobStatus(pool.getJobStatus());
        }

        poolResponse.setObjectName("storagepool");
        return poolResponse;
    }

    @Override
    public StoragePoolResponse setStoragePoolResponse(final StoragePoolResponse response, final StoragePoolJoinVO sp) {
        final String tag = sp.getTag();
        if (tag != null) {
            if (response.getTags() != null && response.getTags().length() > 0) {
                response.setTags(response.getTags() + "," + tag);
            } else {
                response.setTags(tag);
            }
        }
        return response;
    }

    @Override
    public StoragePoolResponse newStoragePoolForMigrationResponse(final StoragePoolJoinVO pool) {
        final StoragePoolResponse poolResponse = new StoragePoolResponse();
        poolResponse.setId(pool.getUuid());
        poolResponse.setName(pool.getName());
        poolResponse.setState(pool.getStatus());
        String path = pool.getPath();
        //cifs store may contain password entry, remove the password
        path = StringUtils.cleanString(path);
        poolResponse.setPath(path);
        poolResponse.setIpAddress(pool.getHostAddress());
        poolResponse.setZoneId(pool.getZoneUuid());
        poolResponse.setZoneName(pool.getZoneName());
        if (pool.getPoolType() != null) {
            poolResponse.setType(pool.getPoolType().toString());
        }
        poolResponse.setPodId(pool.getPodUuid());
        poolResponse.setPodName(pool.getPodName());
        poolResponse.setCreated(pool.getCreated());
        poolResponse.setScope(pool.getScope().toString());
        if (pool.getHypervisor() != null) {
            poolResponse.setHypervisor(pool.getHypervisor().toString());
        }

        final long allocatedSize = pool.getUsedCapacity();
        poolResponse.setDiskSizeTotal(pool.getCapacityBytes());
        poolResponse.setDiskSizeAllocated(allocatedSize);
        poolResponse.setCapacityIops(pool.getCapacityIops());
        poolResponse.setOverProvisionFactor(Double.toString(CapacityManager.StorageOverprovisioningFactor.valueIn(pool.getId())));

        // TODO: StatsCollector does not persist data
        final StorageStats stats = ApiDBUtils.getStoragePoolStatistics(pool.getId());
        if (stats != null) {
            final Long used = stats.getByteUsed();
            poolResponse.setDiskSizeUsed(used);
        }

        poolResponse.setClusterId(pool.getClusterUuid());
        poolResponse.setClusterName(pool.getClusterName());
        poolResponse.setTags(pool.getTag());

        // set async job
        poolResponse.setJobId(pool.getJobUuid());
        poolResponse.setJobStatus(pool.getJobStatus());

        poolResponse.setObjectName("storagepool");
        return poolResponse;
    }

    @Override
    public StoragePoolResponse setStoragePoolForMigrationResponse(final StoragePoolResponse response, final StoragePoolJoinVO sp) {
        final String tag = sp.getTag();
        if (tag != null) {
            if (response.getTags() != null && response.getTags().length() > 0) {
                response.setTags(response.getTags() + "," + tag);
            } else {
                response.setTags(tag);
            }
        }
        return response;
    }

    @Override
    public List<StoragePoolJoinVO> newStoragePoolView(final StoragePool host) {
        final SearchCriteria<StoragePoolJoinVO> sc = spIdSearch.create();
        sc.setParameters("id", host.getId());
        return searchIncludingRemoved(sc, null, null, false);
    }

    @Override
    public List<StoragePoolJoinVO> searchByIds(final Long... spIds) {
        // set detail batch query size
        int DETAILS_BATCH_SIZE = 2000;
        final String batchCfg = _configDao.getValue("detail.batch.query.size");
        if (batchCfg != null) {
            DETAILS_BATCH_SIZE = Integer.parseInt(batchCfg);
        }
        // query details by batches
        final List<StoragePoolJoinVO> uvList = new ArrayList<>();
        // query details by batches
        int curr_index = 0;
        if (spIds.length > DETAILS_BATCH_SIZE) {
            while ((curr_index + DETAILS_BATCH_SIZE) <= spIds.length) {
                final Long[] ids = new Long[DETAILS_BATCH_SIZE];
                for (int k = 0, j = curr_index; j < curr_index + DETAILS_BATCH_SIZE; j++, k++) {
                    ids[k] = spIds[j];
                }
                final SearchCriteria<StoragePoolJoinVO> sc = spSearch.create();
                sc.setParameters("idIN", ids);
                final List<StoragePoolJoinVO> vms = searchIncludingRemoved(sc, null, null, false);
                if (vms != null) {
                    uvList.addAll(vms);
                }
                curr_index += DETAILS_BATCH_SIZE;
            }
        }
        if (curr_index < spIds.length) {
            final int batch_size = (spIds.length - curr_index);
            // set the ids value
            final Long[] ids = new Long[batch_size];
            for (int k = 0, j = curr_index; j < curr_index + batch_size; j++, k++) {
                ids[k] = spIds[j];
            }
            final SearchCriteria<StoragePoolJoinVO> sc = spSearch.create();
            sc.setParameters("idIN", ids);
            final List<StoragePoolJoinVO> vms = searchIncludingRemoved(sc, null, null, false);
            if (vms != null) {
                uvList.addAll(vms);
            }
        }
        return uvList;
    }
}
