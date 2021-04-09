package gy.zr.twitchdropcampaignsrest

import org.springframework.data.domain.Sort
import org.springframework.data.repository.CrudRepository
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "games")
data class Game(
    @Id val id: Int,
    val name: String,
    @Column(name = "box_art_url")
    val boxArtUrl: String
)

@Entity
@Table(name = "drops")
data class DropCampaign(
    @Id val id: String,
    val name: String,
    @ManyToOne
    @JoinColumn(name = "game_id")
    val game: Game,
    val status: String,
    val started: Date,
    val ended: Date
)

interface DropCampaignRepository : CrudRepository<DropCampaign, String> {
    fun findAllByStatusIs(status: String): List<DropCampaign>
    fun findAllByStatusIs(status: String, sort: Sort): List<DropCampaign>
}

@RestController
@CrossOrigin(origins = ["http://localhost:4200"])
class DropCampaignController(val repository: DropCampaignRepository) {

    @GetMapping("/all")
    fun all(): List<DropCampaign> {
        return repository.findAll().toList()
    }

    @GetMapping("/active")
    fun activeCampaigns(): List<DropCampaign> {
        return repository.findAllByStatusIs("ACTIVE", Sort.by(Sort.Direction.DESC, "started")) // Recently started first
    }

    @GetMapping("/upcoming")
    fun upcomingCampaigns(): List<DropCampaign> {
        return repository.findAllByStatusIs("UPCOMING", Sort.by(Sort.Direction.ASC, "started")) // Soon to start first
    }

    @GetMapping("/expired")
    fun expiredCampaigns(): List<DropCampaign> {
        return repository.findAllByStatusIs("EXPIRED", Sort.by(Sort.Direction.DESC, "ended")) // Recently ended first
    }
}