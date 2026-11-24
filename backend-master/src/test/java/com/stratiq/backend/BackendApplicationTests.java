package com.stratiq.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class BackendApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void contextLoads() {
		// Smoke test: verify application context loads successfully
		assertNotNull(applicationContext);
	}

	@Test
	void allRequiredBeansAreLoaded() {
		// Verify critical service beans are loaded
		assertNotNull(applicationContext.getBean("userServiceImpl"));
		assertNotNull(applicationContext.getBean("orderServiceImpl"));
		assertNotNull(applicationContext.getBean("warehouseServiceImpl"));
		assertNotNull(applicationContext.getBean("itemServiceImpl"));
		assertNotNull(applicationContext.getBean("productionStepServiceImpl"));
		assertNotNull(applicationContext.getBean("auditServiceImpl"));
	}

	@Test
	void allRequiredRepositoriesAreLoaded() {
		// Verify critical repository beans are discovered
		assertNotNull(applicationContext.getBean("appUserRepository"));
		assertNotNull(applicationContext.getBean("orderRepository"));
		assertNotNull(applicationContext.getBean("warehouseRepository"));
		assertNotNull(applicationContext.getBean("itemRepository"));
		assertNotNull(applicationContext.getBean("productionStepRepository"));
		assertNotNull(applicationContext.getBean("auditLogRepository"));
	}

	@Test
	void allRequiredControllersAreLoaded() {
		// Verify critical controller beans are loaded
		assertNotNull(applicationContext.getBean("userController"));
		assertNotNull(applicationContext.getBean("orderController"));
		assertNotNull(applicationContext.getBean("warehouseController"));
		assertNotNull(applicationContext.getBean("itemController"));
		assertNotNull(applicationContext.getBean("productionStepController"));
		assertNotNull(applicationContext.getBean("auditController"));
	}
}
