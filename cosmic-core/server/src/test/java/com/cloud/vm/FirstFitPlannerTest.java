package com.cloud.vm;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cloud.capacity.Capacity;
import com.cloud.capacity.CapacityManager;
import com.cloud.capacity.dao.CapacityDao;
import com.cloud.configuration.Config;
import com.cloud.context.CallContext;
import com.cloud.dc.ClusterDetailsDao;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.deploy.DataCenterDeployment;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.deploy.FirstFitPlanner;
import com.cloud.engine.subsystem.api.storage.DataStoreManager;
import com.cloud.exception.InsufficientServerCapacityException;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.gpu.dao.HostGpuGroupsDao;
import com.cloud.host.Host;
import com.cloud.host.dao.HostDao;
import com.cloud.host.dao.HostTagsDao;
import com.cloud.resource.ResourceManager;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.service.dao.ServiceOfferingDetailsDao;
import com.cloud.storage.StorageManager;
import com.cloud.storage.dao.DiskOfferingDao;
import com.cloud.storage.dao.GuestOSCategoryDao;
import com.cloud.storage.dao.GuestOSDao;
import com.cloud.storage.dao.StoragePoolHostDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.storage.datastore.db.PrimaryDataStoreDao;
import com.cloud.test.utils.SpringUtils;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.AccountVO;
import com.cloud.utils.Pair;
import com.cloud.utils.component.ComponentContext;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.dao.VMInstanceDao;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class FirstFitPlannerTest {

    private static final long domainId = 1L;
    @Inject
    FirstFitPlanner planner = new FirstFitPlanner();
    @Inject
    DataCenterDao dcDao;
    @Inject
    ClusterDao clusterDao;
    @Inject
    UserVmDao vmDao;
    @Inject
    ConfigurationDao configDao;
    @Inject
    CapacityDao capacityDao;
    @Inject
    AccountManager accountMgr;
    @Inject
    ServiceOfferingDao serviceOfferingDao;
    @Inject
    ServiceOfferingDetailsDao serviceOfferingDetailsDao;
    @Inject
    HostGpuGroupsDao hostGpuGroupsDao;
    @Inject
    HostTagsDao hostTagsDao;
    long dataCenterId = 1L;
    long accountId = 1L;
    long offeringId = 12L;
    int noOfCpusInOffering = 1;
    int cpuSpeedInOffering = 500;
    int ramInOffering = 512;
    AccountVO acct = new AccountVO(accountId);

    @Before
    public void setUp() {
        when(configDao.getValue(Mockito.anyString())).thenReturn(null);
        when(configDao.getValue(Config.ImplicitHostTags.key())).thenReturn("GPU");

        ComponentContext.initComponentsLifeCycle();
        acct.setType(Account.ACCOUNT_TYPE_ADMIN);
        acct.setAccountName("user1");
        acct.setDomainId(domainId);
        acct.setId(accountId);
    }

    @After
    public void tearDown() {
        CallContext.unregister();
    }

    @Test
    public void checkClusterReorderingBasedOnImplicitHostTags() throws InsufficientServerCapacityException {
        final VirtualMachineProfileImpl vmProfile = mock(VirtualMachineProfileImpl.class);
        final DataCenterDeployment plan = mock(DataCenterDeployment.class);
        final ExcludeList avoids = mock(ExcludeList.class);
        initializeForTest(vmProfile, plan, avoids);

        final List<Long> clusterList = planner.orderClusters(vmProfile, plan, avoids);
        final List<Long> reorderedClusterList = new ArrayList<>();
        reorderedClusterList.add(4L);
        reorderedClusterList.add(3L);
        reorderedClusterList.add(1L);
        reorderedClusterList.add(5L);
        reorderedClusterList.add(6L);
        reorderedClusterList.add(2L);

        assertTrue("Reordered cluster list is not ownering the implict host tags", (clusterList.equals(reorderedClusterList)));
    }

    private void initializeForTest(final VirtualMachineProfileImpl vmProfile, final DataCenterDeployment plan, final ExcludeList avoids) {
        final DataCenterVO mockDc = mock(DataCenterVO.class);
        final VMInstanceVO vm = mock(VMInstanceVO.class);
        final UserVmVO userVm = mock(UserVmVO.class);
        final ServiceOfferingVO offering = mock(ServiceOfferingVO.class);

        final AccountVO account = mock(AccountVO.class);
        when(account.getId()).thenReturn(accountId);
        when(account.getAccountId()).thenReturn(accountId);
        when(vmProfile.getOwner()).thenReturn(account);
        when(vmProfile.getVirtualMachine()).thenReturn(vm);
        when(vmProfile.getId()).thenReturn(12L);
        when(vmDao.findById(12L)).thenReturn(userVm);
        when(userVm.getAccountId()).thenReturn(accountId);

        when(vm.getDataCenterId()).thenReturn(dataCenterId);
        when(dcDao.findById(1L)).thenReturn(mockDc);
        when(avoids.shouldAvoid(mockDc)).thenReturn(false);
        when(plan.getDataCenterId()).thenReturn(dataCenterId);
        when(plan.getClusterId()).thenReturn(null);
        when(plan.getPodId()).thenReturn(null);

        // Mock offering details.
        when(vmProfile.getServiceOffering()).thenReturn(offering);
        when(offering.getId()).thenReturn(offeringId);
        when(vmProfile.getServiceOfferingId()).thenReturn(offeringId);
        when(offering.getCpu()).thenReturn(noOfCpusInOffering);
        when(offering.getSpeed()).thenReturn(cpuSpeedInOffering);
        when(offering.getRamSize()).thenReturn(ramInOffering);

        final List<Long> clustersWithEnoughCapacity = new ArrayList<>();
        clustersWithEnoughCapacity.add(1L);
        clustersWithEnoughCapacity.add(2L);
        clustersWithEnoughCapacity.add(3L);
        clustersWithEnoughCapacity.add(4L);
        clustersWithEnoughCapacity.add(5L);
        clustersWithEnoughCapacity.add(6L);

        when(
                capacityDao.listClustersInZoneOrPodByHostCapacities(dataCenterId, noOfCpusInOffering * cpuSpeedInOffering, ramInOffering * 1024L * 1024L,
                        Capacity.CAPACITY_TYPE_CPU, true)).thenReturn(clustersWithEnoughCapacity);

        final Map<Long, Double> clusterCapacityMap = new HashMap<>();
        clusterCapacityMap.put(1L, 2048D);
        clusterCapacityMap.put(2L, 2048D);
        clusterCapacityMap.put(3L, 2048D);
        clusterCapacityMap.put(4L, 2048D);
        clusterCapacityMap.put(5L, 2048D);
        clusterCapacityMap.put(6L, 2048D);

        final Pair<List<Long>, Map<Long, Double>> clustersOrderedByCapacity = new Pair<>(clustersWithEnoughCapacity, clusterCapacityMap);
        when(capacityDao.orderClustersByAggregateCapacity(dataCenterId, Capacity.CAPACITY_TYPE_CPU, true)).thenReturn(clustersOrderedByCapacity);

        final List<Long> disabledClusters = new ArrayList<>();
        final List<Long> clustersWithDisabledPods = new ArrayList<>();
        when(clusterDao.listDisabledClusters(dataCenterId, null)).thenReturn(disabledClusters);
        when(clusterDao.listClustersWithDisabledPods(dataCenterId)).thenReturn(clustersWithDisabledPods);

        final List<Long> hostList0 = new ArrayList<>();
        final List<Long> hostList1 = new ArrayList<>();
        final List<Long> hostList2 = new ArrayList<>();
        final List<Long> hostList3 = new ArrayList<>();
        final List<Long> hostList4 = new ArrayList<>();
        final List<Long> hostList5 = new ArrayList<>();
        final List<Long> hostList6 = new ArrayList<>();
        hostList0.add(new Long(1));
        hostList1.add(new Long(10));
        hostList2.add(new Long(11));
        hostList3.add(new Long(12));
        hostList4.add(new Long(13));
        hostList5.add(new Long(14));
        hostList6.add(new Long(15));
        final String[] implicitHostTags = {"GPU"};
        final int ramInBytes = ramInOffering * 1024 * 1024;
        when(serviceOfferingDetailsDao.findDetail(Matchers.anyLong(), anyString())).thenReturn(null);
        when(hostGpuGroupsDao.listHostIds()).thenReturn(hostList0);
        when(capacityDao.listHostsWithEnoughCapacity(noOfCpusInOffering * cpuSpeedInOffering, ramInBytes, new Long(1), Host.Type.Routing.toString())).thenReturn(hostList1);
        when(capacityDao.listHostsWithEnoughCapacity(noOfCpusInOffering * cpuSpeedInOffering, ramInBytes, new Long(2), Host.Type.Routing.toString())).thenReturn(hostList2);
        when(capacityDao.listHostsWithEnoughCapacity(noOfCpusInOffering * cpuSpeedInOffering, ramInBytes, new Long(3), Host.Type.Routing.toString())).thenReturn(hostList3);
        when(capacityDao.listHostsWithEnoughCapacity(noOfCpusInOffering * cpuSpeedInOffering, ramInBytes, new Long(4), Host.Type.Routing.toString())).thenReturn(hostList4);
        when(capacityDao.listHostsWithEnoughCapacity(noOfCpusInOffering * cpuSpeedInOffering, ramInBytes, new Long(5), Host.Type.Routing.toString())).thenReturn(hostList5);
        when(capacityDao.listHostsWithEnoughCapacity(noOfCpusInOffering * cpuSpeedInOffering, ramInBytes, new Long(6), Host.Type.Routing.toString())).thenReturn(hostList6);
        when(hostTagsDao.getDistinctImplicitHostTags(hostList1, implicitHostTags)).thenReturn(Arrays.asList("abc", "pqr", "xyz"));
        when(hostTagsDao.getDistinctImplicitHostTags(hostList2, implicitHostTags)).thenReturn(Arrays.asList("abc", "123", "pqr", "456", "xyz"));
        when(hostTagsDao.getDistinctImplicitHostTags(hostList3, implicitHostTags)).thenReturn(Arrays.asList("abc", "pqr"));
        when(hostTagsDao.getDistinctImplicitHostTags(hostList4, implicitHostTags)).thenReturn(Arrays.asList("abc"));
        when(hostTagsDao.getDistinctImplicitHostTags(hostList5, implicitHostTags)).thenReturn(Arrays.asList("abc", "pqr", "xyz"));
        when(hostTagsDao.getDistinctImplicitHostTags(hostList6, implicitHostTags)).thenReturn(Arrays.asList("abc", "123", "pqr", "xyz"));
    }

    @Configuration
    @ComponentScan(basePackageClasses = {FirstFitPlanner.class},
            includeFilters = {@Filter(value = TestConfiguration.Library.class, type = FilterType.CUSTOM)},
            useDefaultFilters = false)
    public static class TestConfiguration extends SpringUtils.CloudStackTestConfiguration {

        @Bean
        public HostDao hostDao() {
            return Mockito.mock(HostDao.class);
        }

        @Bean
        public HostTagsDao hostTagsDao() {
            return Mockito.mock(HostTagsDao.class);
        }

        @Bean
        public HostGpuGroupsDao hostGpuGroupsDao() {
            return Mockito.mock(HostGpuGroupsDao.class);
        }

        @Bean
        public DataCenterDao dcDao() {
            return Mockito.mock(DataCenterDao.class);
        }

        @Bean
        public HostPodDao hostPodDao() {
            return Mockito.mock(HostPodDao.class);
        }

        @Bean
        public ClusterDao clusterDao() {
            return Mockito.mock(ClusterDao.class);
        }

        @Bean
        public GuestOSDao guestOsDao() {
            return Mockito.mock(GuestOSDao.class);
        }

        @Bean
        public GuestOSCategoryDao guestOsCategoryDao() {
            return Mockito.mock(GuestOSCategoryDao.class);
        }

        @Bean
        public DiskOfferingDao diskOfferingDao() {
            return Mockito.mock(DiskOfferingDao.class);
        }

        @Bean
        public StoragePoolHostDao storagePoolHostDao() {
            return Mockito.mock(StoragePoolHostDao.class);
        }

        @Bean
        public UserVmDao userVmDao() {
            return Mockito.mock(UserVmDao.class);
        }

        @Bean
        public VMInstanceDao vmInstanceDao() {
            return Mockito.mock(VMInstanceDao.class);
        }

        @Bean
        public VolumeDao volumeDao() {
            return Mockito.mock(VolumeDao.class);
        }

        @Bean
        public CapacityManager capacityManager() {
            return Mockito.mock(CapacityManager.class);
        }

        @Bean
        public ConfigurationDao configurationDao() {
            return Mockito.mock(ConfigurationDao.class);
        }

        @Bean
        public PrimaryDataStoreDao primaryDataStoreDao() {
            return Mockito.mock(PrimaryDataStoreDao.class);
        }

        @Bean
        public CapacityDao capacityDao() {
            return Mockito.mock(CapacityDao.class);
        }

        @Bean
        public AccountManager accountManager() {
            return Mockito.mock(AccountManager.class);
        }

        @Bean
        public StorageManager storageManager() {
            return Mockito.mock(StorageManager.class);
        }

        @Bean
        public DataStoreManager dataStoreManager() {
            return Mockito.mock(DataStoreManager.class);
        }

        @Bean
        public ClusterDetailsDao clusterDetailsDao() {
            return Mockito.mock(ClusterDetailsDao.class);
        }

        @Bean
        public ServiceOfferingDao serviceOfferingDao() {
            return Mockito.mock(ServiceOfferingDao.class);
        }

        @Bean
        public ServiceOfferingDetailsDao serviceOfferingDetailsDao() {
            return Mockito.mock(ServiceOfferingDetailsDao.class);
        }

        @Bean
        public ResourceManager resourceManager() {
            return Mockito.mock(ResourceManager.class);
        }

        public static class Library implements TypeFilter {
            @Override
            public boolean match(final MetadataReader mdr, final MetadataReaderFactory arg1) throws IOException {
                final ComponentScan cs = TestConfiguration.class.getAnnotation(ComponentScan.class);
                return SpringUtils.includedInBasePackageClasses(mdr.getClassMetadata().getClassName(), cs);
            }
        }
    }
}
