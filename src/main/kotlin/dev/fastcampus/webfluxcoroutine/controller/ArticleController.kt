package dev.fastcampus.webfluxcoroutine.controller

import dev.fastcampus.webfluxcoroutine.model.Article
import dev.fastcampus.webfluxcoroutine.service.ArticleService
import dev.fastcampus.webfluxcoroutine.service.QryArticle
import dev.fastcampus.webfluxcoroutine.service.ReqCreate
import dev.fastcampus.webfluxcoroutine.service.ReqUpdate
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name="주문", description = "사용자 주문을 처리하는 API")
@RestController
@RequestMapping("/article")
class ArticleController(
    private val service: ArticleService,
) {

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody request: ReqCreate): Article {
        return service.create(request)
    }

    @Operation(summary="주문 단건조회")
    @GetMapping("/{id}")
    suspend fun get(@PathVariable id: Long): Article {
        return service.get(id)
    }

//    @GetMapping("/all")
//    suspend fun getAll(@RequestParam title: String?): Flow<Article> {
//        return service.getAll(title)
//    }

    @Operation(summary="주문 이력 조회", description="대고객용")
    @GetMapping("/all")
    suspend fun getAll(request: QryArticle): Flow<Article> {
//        return service.getAll(request)
        return service.getAllCached(request)
    }

    @PutMapping("/{id}")
    suspend fun update(@PathVariable id: Long, @RequestBody request: ReqUpdate): Article {
        return service.update(id, request)
    }

    @DeleteMapping("/{id}")
    suspend fun delete(@PathVariable id: Long) {
        return service.delete(id)
    }

}