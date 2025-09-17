package com.example.myprofile.network.repositories

import com.example.myprofile.models.PrinterInfoDto
import com.example.myprofile.models.SavePrinterMacDto
import com.example.myprofile.network.ApiService

class PrinterRepository(private val api: ApiService) {
    suspend fun getPrinterInfo(): PrinterInfoDto = api.getPrinterInfo()
    suspend fun savePrinterMac(mac: String) { api.savePrinterMac(SavePrinterMacDto(mac)) }
}