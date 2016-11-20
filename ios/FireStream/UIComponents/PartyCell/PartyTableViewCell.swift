//
//  PartyTableViewCell.swift
//  FireStream
//
//  Created by Caleb Adcock on 11/19/16.
//  Copyright © 2016 Syntonic LLC. All rights reserved.
//

import UIKit
import Material

class PartyTableViewCell: TableViewCell {
    @IBOutlet weak var imgAlbumArt: UIImageView!
    @IBOutlet weak var lblName: UILabel!
    @IBOutlet weak var lblHost: UILabel!
    @IBOutlet weak var lblAttendees: UILabel!
    @IBOutlet weak var imgPeople: UIImageView!
    @IBOutlet weak var imgLock: UIImageView!
    @IBOutlet weak var constraintImgLock: NSLayoutConstraint!
    
    var showLock: Bool = false {
        didSet {
            constraintImgLock.constant = showLock ? 24 : 0
            imgLock.isHidden = !showLock
        }
    }

    override func awakeFromNib() {
        super.awakeFromNib()
        imgPeople.image = imgPeople.image?.withRenderingMode(.alwaysTemplate)
        imgLock.image = imgLock.image?.withRenderingMode(.alwaysTemplate)
        showLock = showLock ? true : false
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
