//
//  PartySearchViewController.swift
//  FireStream
//
//  Created by Caleb Adcock on 11/19/16.
//  Copyright © 2016 Syntonic LLC. All rights reserved.
//

import UIKit
import Firebase
import SDWebImage
import Spotify

class PartySearchViewController: MaterialViewController, UITableViewDelegate, UITableViewDataSource, UISearchResultsUpdating {
    
    let FIREBASE_DATABASE_TABLE_PARTIES = "parites"
    
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var superViewSearchBar: UIView!
    
    @IBAction func BtnAddPressed() {
        if SpotifyInterface.IsInitilized() {
            ControllerInterface.DoSegue(segueCommand: .ToCreateParty, viewController: self, segueType: .Modal)
        } else {
            guard let controller = SpotifyInterface.Authenticate() else {
                ControllerInterface.DoSegue(segueCommand: .ToCreateParty, viewController: self, segueType: .Modal)
                return
            }
            self.present(controller, animated: true, completion: nil)
        }
    }
    
    let searchController = UISearchController(searchResultsController: nil)
    var parties: [Party] = [Party]()

    override var showAppBarShadow: Bool {
        get { return false }
        set { }
    }
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        (self.navigationController as? MaterialNavigationController)?.updateAppBar()
        tableView.tableFooterView = UIView(frame: CGRect.zero)
        tableView.backgroundColor = UIColor.colorBg
        tableView.register(UINib(nibName: "PartyTableViewCell", bundle: nil), forCellReuseIdentifier: "partyCell")
        
        searchController.searchResultsUpdater = self
        searchController.hidesNavigationBarDuringPresentation = false
        searchController.dimsBackgroundDuringPresentation = false
        searchController.searchBar.sizeToFit()
        superViewSearchBar.addSubview(searchController.searchBar)
        self.definesPresentationContext = true
        
        if let textField = searchController.searchBar.value(forKey: "searchField") as? UITextField {
            textField.adjustsFontSizeToFitWidth = true
            textField.borderStyle = .none
            textField.textColor = .black
            textField.tintColor = .black
            textField.backgroundColor = .white
            textField.layer.cornerRadius = 2
        }
        searchController.searchBar.layer.cornerRadius = 2
        searchController.searchBar.searchBarStyle = .minimal
        searchController.searchBar.tintColor = .white
        searchController.searchBar.backgroundColor = nil
        
        tableView.rowHeight = UITableViewAutomaticDimension
        tableView.estimatedRowHeight = 90
        
        subscribeToParties()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    

    // MARK: Fetch Data
    func subscribeToParties() {
        parties.removeAll()
        
        let db = Utils.getDatabase().reference(fromURL: "https://firestream-4e998.firebaseio.com/parties")
        let q = db.queryOrdered(byChild: "attendees").queryLimited(toLast: 50)
        
        q.observe(.childAdded, with: { snapshot in
            guard let partyDict = snapshot.value as? [String:Any] else { return }
            let party = Party(dict: partyDict)
            self.parties.append(party)
            self.tableView.reloadData()
        }, withCancel: { (error) in
            print("Database error: \(error)")
        })
        q.observe(.childChanged, with: { (snapshot) in
            guard let partyDict = snapshot.value as? [String:Any] else { return }
            let party = Party(dict: partyDict)
            for i in 0..<self.parties.count {
                if self.parties[i].id == party.id {
                    self.parties[i] = party
                    self.tableView.reloadRows(at: [IndexPath(row: i, section: 0)], with: .automatic)
                    break
                }
            }
        }, withCancel: { (error) in
            print("Database error: \(error)")
        })
    
        q.observe(.childRemoved, with: { (snapshot) in
            guard let partyDict = snapshot.value as? [String:Any] else { return }
            let party = Party(dict: partyDict)
            for i in 0..<self.parties.count {
                if self.parties[i].id == party.id {
                    self.parties.remove(at: i)
                    self.tableView.deleteRows(at: [IndexPath(row: i, section: 0)], with: .automatic)
                    break
                }
            }
        }, withCancel: { (error) in
            print("Database error: \(error)")
        })
        
        q.observe(.childMoved, with: { (snapshot) in
            
        }, withCancel: { (error) in
            print("Database error: \(error)")
        })
    }
    
    // MARK: UITableViewDataSource
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return parties.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "partyCell") as! PartyTableViewCell
        guard let party = parties[safe: indexPath.row] else { return cell }
        cell.lblName.text = party.name
        cell.lblHost.text = "Hosted by: \(party.hostName)"
        cell.lblAttendees.text = "\(party.attendees < 1000 ? "\(party.attendees)" : ">999")"
        if let song = party.queue.first {
            cell.imgAlbumArt.sd_setImage(with: URL(string: song.albumUrl))
        }
        
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        ControllerInterface.DoSegue(segueCommand: .ToParty, viewController: self, segueType: .Show, extraDataObject: parties[indexPath.row])
    }
    
    // MARK: UISearchResultsUpdating
    func updateSearchResults(for searchController: UISearchController) {
        
    }
}
