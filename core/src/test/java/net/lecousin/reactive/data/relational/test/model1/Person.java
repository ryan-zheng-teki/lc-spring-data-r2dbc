package net.lecousin.reactive.data.relational.test.model1;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import net.lecousin.reactive.data.relational.annotations.ForeignKey;
import net.lecousin.reactive.data.relational.annotations.ForeignTable;
import net.lecousin.reactive.data.relational.annotations.GeneratedValue;
import net.lecousin.reactive.data.relational.annotations.Index;
import net.lecousin.reactive.data.relational.annotations.Indexes;
import reactor.core.publisher.Mono;

@Table
@Indexes({
	@Index(name = "indexFirstName", properties = { "firstName" }, unique = false),
	@Index(name = "indexLastName", properties = { "lastName" }, unique = false),
	@Index(name = "indexName", properties = { "firstName", "lastName" }, unique = true)
})
public class Person {

	@Id @GeneratedValue
	private Long id;
	
	@Version
	private int version;

	@Column
	private String firstName;
	
	@Column
	private String lastName;
	
	@ForeignTable(joinKey = "person", optional = true)
	private Employee job;

	@ForeignKey(optional = true, cascadeDelete = true)
	private PostalAddress address;
	
	@ForeignTable(joinKey = "owner", optional = true)
	private Company owningCompany;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Employee getJob() {
		return job;
	}

	public void setJob(Employee job) {
		this.job = job;
	}

	public PostalAddress getAddress() {
		return address;
	}

	public void setAddress(PostalAddress address) {
		this.address = address;
	}
	
	public boolean entityLoaded() {
		return false;
	}
	
	public Mono<Person> loadEntity() {
		return null;
	}
	
	public Mono<Employee> lazyGetJob() {
		return null;
	}

	public Company getOwningCompany() {
		return owningCompany;
	}

	public void setOwningCompany(Company owningCompany) {
		this.owningCompany = owningCompany;
	}

}
