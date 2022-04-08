package io.github.jokoroukwu.zephyrng.datasetindexprovider

interface TestDataIndexProvider {

    /**
     * Poll the index for the provided data set
     *
     * @return data set index or null if none exists for provided data set
     * @see [TestDataID]
     * @see [TestDataIndexProviderBase]
     */
    fun pollDataSetIndex(testDataID: TestDataID): Int?
}
