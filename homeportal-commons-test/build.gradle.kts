dependencies {
    api("org.springframework:spring-context")

    // MvcTestUtils uses FlashMap and ModelAndView from spring-webmvc plus MvcResult
    // from spring-test. Both 'provided': a test-utility module has no business adding
    // anything to the consumer's production classpath.
    compileOnly("org.springframework:spring-webmvc")
    compileOnly("org.springframework:spring-test")
}
