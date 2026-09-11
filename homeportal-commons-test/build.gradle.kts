dependencies {
    api("org.springframework:spring-context")

    // MvcTestUtils uses FlashMap and ModelAndView from spring-webmvc plus MvcResult
    // from spring-test. Both 'provided': a test-utility module has no business adding
    // anything to the consumer's production classpath.
    provided("org.springframework:spring-webmvc")
    provided("org.springframework:spring-test")
}
