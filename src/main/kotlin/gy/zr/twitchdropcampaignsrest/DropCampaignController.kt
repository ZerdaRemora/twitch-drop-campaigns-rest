package gy.zr.twitchdropcampaignsrest

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.domain.Sort
import org.springframework.data.repository.CrudRepository
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
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


@Entity
@Table(name = "dropbenefits")
data class DropBenefits(
    @Id val id: String,
    val name: String,
    @ManyToOne
    @JoinColumn(name = "drop_id")
    val dropCampaign: DropCampaign,
    @Column(name = "required_minutes_watched")
    val requiredMinutesWatched: Int,
    @Column(name = "entitlement_limit")
    val entitlementLimit: Int,
    @Column(name = "benefit_art_url")
    val benefitArtUrl: String,
    @ManyToMany
    @JoinTable(name = "dropbenefitprereqs", joinColumns = [JoinColumn(name = "benefit_id")], inverseJoinColumns = [JoinColumn(name = "pre_req_benefit_id")])
    @JsonIgnoreProperties("prerequisiteDrops") // Makes it so that a drop will only show it's IMMEDIATE pre-requisites. (If A->B->C, this only shows A->B and B->C)
    val prerequisiteDrops: Set<DropBenefits>
)

interface DropCampaignRepository : CrudRepository<DropCampaign, String> {
    fun findAllByStatusIs(status: String): List<DropCampaign>
    fun findAllByStatusIs(status: String, sort: Sort): List<DropCampaign>
}

interface DropBenefitsRepository : CrudRepository<DropBenefits, String> {
    fun findAllByDropCampaign_Id(id: String): List<DropBenefits>
}

@RestController
@CrossOrigin(origins = ["http://localhost:4200", "https://localhost:4200", "https://zerdaremora.github.io"])
class DropCampaignController(val repository: DropCampaignRepository, val benefitsRepository: DropBenefitsRepository) {

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

    @GetMapping("/allbenefits")
    fun allBenefits(): List<DropBenefits> {
        return benefitsRepository.findAll().toList()
    }

    @GetMapping("/benefitsfor")
    fun benefitsForCampaign(@RequestParam campaignId: String): List<DropBenefits> {
        return benefitsRepository.findAllByDropCampaign_Id(campaignId)
    }
}