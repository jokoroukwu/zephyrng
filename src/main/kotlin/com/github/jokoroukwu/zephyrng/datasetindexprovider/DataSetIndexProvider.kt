package com.github.jokoroukwu.zephyrng.datasetindexprovider

interface DataSetIndexProvider {

    /**
     * Poll the index for the provided data set
     *
     * @return data set index or null if none exists for provided data set
     * @see [DataSetID]
     * @see [DataSetIndexProviderImpl]
     */
    fun pollDataSetIndex(dataSetID: DataSetID): Int?
}
